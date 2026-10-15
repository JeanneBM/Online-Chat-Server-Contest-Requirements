package pl.workshop.chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatFlowsTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    record Account(String username, String token) {}

    private JsonNode json(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private Account register() throws Exception {
        String name = "user-" + UUID.randomUUID().toString().substring(0, 8);
        JsonNode response = postJson("/api/auth/register", null,
                Map.of("username", name, "email", name + "@local.test", "password", "test-password"));
        assertEquals(name, response.get("username").asText());
        return new Account(name, response.get("token").asText());
    }

    private JsonNode postJson(String path, Account account, Object body) throws Exception {
        var request = post(path).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body));
        if (account != null) request.header("Authorization", "Bearer " + account.token());
        return json(mvc.perform(request).andExpect(status().isOk()).andReturn());
    }

    private JsonNode getJson(String path, Account account) throws Exception {
        return json(mvc.perform(get(path).header("Authorization", "Bearer " + account.token()))
                .andExpect(status().isOk()).andReturn());
    }

    @Test
    void registrationLoginAndPublicRoomWorkWithoutSeedData() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/api/friends")).andExpect(status().isUnauthorized());
        Account user = register();
        JsonNode login = postJson("/api/auth/login", null,
                Map.of("email", user.username() + "@local.test", "password", "test-password"));
        assertEquals(user.username(), login.get("username").asText());
        boolean publicFound = false;
        for (JsonNode room : getJson("/api/rooms/public", user)) {
            if (room.get("name").asText().equals("public")) publicFound = true;
        }
        assertTrue(publicFound);
    }

    @Test
    void roomsReturnFiniteHistoryWithUsernamesAndDeletePersistently() throws Exception {
        Account owner = register();
        Account member = register();
        JsonNode room = postJson("/api/rooms", owner, Map.of("name", "room-" + UUID.randomUUID()));
        String roomId = room.get("id").asText();
        postJson("/api/rooms/" + roomId + "/join", member, Map.of());
        JsonNode message = postJson("/api/messages/room/" + roomId, owner, Map.of("content", "hello"));
        String messageId = message.get("id").asText();
        JsonNode history = getJson("/api/messages/room/" + roomId, member);
        assertEquals(1, history.size());
        assertEquals(owner.username(), history.get(0).get("sender").asText());
        assertFalse(history.toString().contains("password"));
        mvc.perform(delete("/api/rooms/" + roomId + "/message/" + messageId)
                        .header("Authorization", "Bearer " + member.token()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/rooms/" + roomId + "/message/" + messageId)
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk());
        assertEquals(0, getJson("/api/messages/room/" + roomId, member).size());
    }

    @Test
    void invitationsEnablePrivateMessagesOnlyAfterAcceptance() throws Exception {
        Account sender = register();
        Account receiver = register();
        mvc.perform(post("/api/messages/private/" + receiver.username())
                        .header("Authorization", "Bearer " + sender.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"blocked\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/friends/request").header("Authorization", "Bearer " + sender.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(Map.of("username", receiver.username()))))
                .andExpect(status().isOk());
        JsonNode requests = getJson("/api/friends/requests/pending", receiver);
        assertEquals(sender.username(), requests.get(0).get("username").asText());
        mvc.perform(post("/api/friends/request/" + requests.get(0).get("id").asText() + "/accept")
                        .header("Authorization", "Bearer " + receiver.token()))
                .andExpect(status().isOk());
        assertEquals(sender.username(), getJson("/api/friends", receiver).get(0).get("username").asText());
        JsonNode message = postJson("/api/messages/private/" + receiver.username(), sender, Map.of("content", "private hello"));
        assertEquals(receiver.username(), message.get("receiver").asText());
        JsonNode history = getJson("/api/messages/private/" + sender.username(), receiver);
        assertEquals(1, history.size());
        assertEquals("private hello", history.get(0).get("content").asText());
        assertEquals(1, getJson("/api/messages/unread-count", receiver).get("privateMessages").asInt());
        mvc.perform(post("/api/messages/private/" + sender.username() + "/read")
                        .header("Authorization", "Bearer " + receiver.token()))
                .andExpect(status().isOk());
        assertEquals(0, getJson("/api/messages/unread-count", receiver).get("privateMessages").asInt());
    }

    @Test
    void fileDownloadsRequireOwnershipOrAccessToTheSharedMessage() throws Exception {
        Account owner = register();
        Account member = register();
        Account outsider = register();
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        JsonNode upload = json(mvc.perform(multipart("/api/attachments").file(file)
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk()).andReturn());
        String url = upload.get("url").asText();
        mvc.perform(get(url).header("Authorization", "Bearer " + outsider.token())).andExpect(status().isForbidden());
        JsonNode room = postJson("/api/rooms", owner, Map.of("name", "files-" + UUID.randomUUID()));
        String id = room.get("id").asText();
        postJson("/api/rooms/" + id + "/join", member, Map.of());
        postJson("/api/messages/room/" + id, owner, Map.of("content", "file", "attachmentUrl", url));
        mvc.perform(get(url).header("Authorization", "Bearer " + member.token()))
                .andExpect(status().isOk()).andExpect(content().bytes("hello".getBytes()));
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
    }
}
