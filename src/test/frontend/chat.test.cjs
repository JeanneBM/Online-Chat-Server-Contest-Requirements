const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const html = fs.readFileSync(path.join(__dirname, '../../main/resources/static/index.html'), 'utf8');
const source = html.match(/<script>([\s\S]*?)<\/script>/)[1];

function app(fetch) {
    const elements = new Map();
    class Element {
        constructor() { this.children = []; this.style = {}; this.dataset = {}; this.value = ''; this.textContent = ''; }
        append(...children) {
            this.children.push(...children);
            for (const child of children) if (child.id) elements.set(child.id, child);
        }
        appendChild(child) { this.append(child); }
        replaceChildren(...children) { this.children = []; this.append(...children); }
        querySelectorAll() { return []; }
        remove() { elements.delete(this.id); }
        addEventListener() {}
        set innerHTML(value) { this.children = []; this.markup = value; }
        get innerHTML() { return this.markup || ''; }
    }
    for (const match of html.matchAll(/id="([^"]+)"/g)) elements.set(match[1], new Element());
    const storage = new Map([['token', 'token'], ['username', 'alice']]);
    const context = vm.createContext({
        console, fetch,
        window: { addEventListener() {} },
        document: {
            hidden: false,
            getElementById: id => elements.get(id) || null,
            createElement: () => new Element(),
            createTextNode: text => ({ textContent: text }),
            addEventListener() {}
        },
        localStorage: { getItem: key => storage.get(key), setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) },
        setTimeout() {}, setInterval() {}, clearInterval() {},
        alert() {}, confirm: () => true
    });
    vm.runInContext(source, context);
    return { run: code => vm.runInContext(code, context), elements, storage };
}

const ok = body => ({ ok: true, status: 200, json: async () => body });

test('failed send reports the server error and preserves the draft', async () => {
    const ui = app(async () => ({ ok: false, status: 403, text: async () => '{"error":"Not friends"}' }));
    ui.elements.get('message-input').value = 'keep this';
    ui.run('currentRoom = "private-bob"; currentPrivatePeer = "bob";');
    await assert.rejects(ui.run('sendMessage()'), /Not friends/);
    assert.equal(ui.elements.get('message-input').value, 'keep this');
});

test('successful private send waits for an HTTP response and clears the draft', async () => {
    const calls = [];
    const ui = app(async (url, options) => {
        calls.push({ url, options });
        return ok({ id: 7, sender: 'alice', receiver: 'bob', content: 'hello' });
    });
    ui.elements.get('message-input').value = 'hello';
    ui.run('currentRoom = "private-bob"; currentPrivatePeer = "bob";');
    await ui.run('sendMessage()');
    assert.equal(calls[0].url, '/api/messages/private/bob');
    assert.equal(calls[0].options.method, 'POST');
    assert.equal(ui.elements.get('message-input').value, '');
    assert.equal(ui.elements.get('messages').children.length, 1);
});

test('opening direct chat does not subscribe or request a fictional room id', async () => {
    const calls = [];
    const ui = app(async url => { calls.push(url); return ok([]); });
    await ui.run('subscribeRoom("private-bob")');
    assert.deepEqual(calls, ['/api/messages/private/bob', '/api/messages/private/bob/read']);
    assert.equal(ui.run('currentRoomId'), null);
});

test('deletion calls the persisted backend operation', async () => {
    const calls = [];
    const ui = app(async (url, options) => { calls.push({ url, options }); return ok({}); });
    ui.run('renderMessageEntry({ id: 8, sender: "alice", roomId: "3", content: "hello" })');
    await ui.run('deleteMessage(8, 3)');
    assert.equal(calls[0].url, '/api/rooms/3/message/8');
    assert.equal(calls[0].options.method, 'DELETE');
    assert.equal(ui.elements.has('message-8'), false);
});

test('received invitations expose an acceptance action', async () => {
    const calls = [];
    const ui = app(async (url, options) => {
        calls.push({ url, options });
        return ok(url.endsWith('/pending') ? [{ id: 12, username: 'bob' }] : []);
    });
    await ui.run('loadPendingFriendRequests()');
    const row = ui.elements.get('pending-friend-requests-list').children[0];
    assert.equal(row.children[1].textContent, 'Accept');
    await row.children[1].onclick();
    assert.equal(calls[1].url, '/api/friends/request/12/accept');
    assert.equal(calls[1].options.method, 'POST');
});

test('a private message from another conversation is not rendered', () => {
    const ui = app(async () => ok({}));
    ui.run('currentRoom = "private-bob"; currentPrivatePeer = "bob"; refreshUnreadCounts = async () => {};');
    ui.run('onPrivateMessageReceived({ body: JSON.stringify({ id: 9, sender: "carol", receiver: "alice", content: "other conversation" }) })');
    assert.equal(ui.elements.get('messages').children.length, 0);
});

test('messages are rendered as text and duplicate live delivery is ignored', () => {
    const ui = app(async () => ok({}));
    ui.run('renderMessageEntry({ id: 1, sender: "<img>", content: "<script>alert(1)</script>" })');
    ui.run('renderMessageEntry({ id: 1, sender: "<img>", content: "<script>alert(1)</script>" })');
    const messages = ui.elements.get('messages');
    assert.equal(messages.children.length, 1);
    assert.equal(messages.children[0].children[1].textContent, '<script>alert(1)</script>');
    assert.equal(messages.children[0].innerHTML, '');
});

test('expired sessions restore the sign-in panel', async () => {
    const ui = app(async () => ({ ok: false, status: 401, text: async () => 'Please sign in again' }));
    await assert.rejects(ui.run('apiFetch("/api/friends")'), /sign in again/);
    assert.equal(ui.storage.has('token'), false);
    assert.equal(ui.elements.get('auth-panel').style.display, 'flex');
});
