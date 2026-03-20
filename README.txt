# Online-Chat-Server-Contest-Requirements

Assignment Instruction
Assignment Instruction

Complete the task from the workshop.
Create public GitHub or GitLab repository with your project.

Project MUST be buildable and runnable by "docker compose up" in the root repository folder.

Publish your solution on GitHub or GitLab and make sure your repository is set to public.

If you have any questions, feel free to ask in Discord: https://discord.gg/nnVDYmFm

1. Introduction

The task is to implement a classic web-based online chat application with support for:

user registration and authentication
public and private chat rooms
one-to-one personal messaging
contacts/friends
file and image sharing
basic moderation and administration features
persistent message history

The application should represent a typical “classic web chat” experience, with straightforward navigation, room lists, contact lists, message history, notifications, and online presence indicators.

The system is intended for moderate scale and should support up to 300 simultaneously connected users.

2. Functional Requirements

2.1. User Accounts and Authentication

2.1.1. Registration

The system shall allow self-registration using:

email
password
unique username

2.1.2. Registration Rules

Email must be unique
Username must be unique
Username is immutable after registration
Email verification is not required

2.1.3. Authentication

The system shall support:

sign in with email and password
sign out (logs out the current browser session only; other active sessions are not affected)
persistent login across browser close/reopen

2.1.4. Password Management

The system shall support:

password reset
password change for logged-in users

No forced periodic password change is required. Passwords must be stored securely in hashed form.

2.1.5. Account Removal

The system shall provide a “Delete account” action.

If a user deletes their account:

their account is removed
only chat rooms owned by that user are deleted
all messages, files, and images in those deleted rooms are deleted permanently
membership in other rooms is removed

2.2. User Presence and Sessions

2.2.1. Presence States

The system shall show contact presence using these statuses:

online
AFK
offline

2.2.2. AFK Rule

A user is considered AFK if they have not interacted with any of their open browser tabs for more than 1 minute.

2.2.3. Multi-Tab Support

The application shall work correctly if the same user opens the chat in multiple browser tabs.

If the user is active in at least one tab, they appear as online to others.
AFK status is set only when all open tabs have been inactive for more than 1 minute.
A user becomes offline only when all browser tabs are closed/offloaded by browser as inactive.

2.2.4. Active Sessions

The user shall be able to view a list of their active sessions, including browser/IP details, and log out selected sessions.

2.3. Contacts / Friends

2.3.1. Friend List

Each user shall have a personal contact/friend list.

2.3.2. Sending Friend Requests

A user shall be able to send a friend request:

by username
from the user list in a chat room

A friend request may include optional text.

2.3.3. Friendship Confirmation

Adding a friend requires confirmation by the recipient.

2.3.4. Removing Friends

A user may remove another user from their friend list.

2.3.5. User-to-User Ban

A user may ban another user.

Ban effect:

the banned user cannot contact the user who banned them in any way
new personal messaging between them is blocked
existing personal message history remains visible but becomes read-only/frozen
friend relationship between the two users is effectively terminated

2.3.6 Personal Messaging Rule

Users may exchange personal messages only if they are friends and neither side has banned the other.

2.4. Chat Rooms

2.4.1. Chat Room Creation

Any registered user may create a chat room.

2.4.2. Chat Room Properties

A chat room shall have:

name
description
visibility: public or private
owner
admins
members
banned users list

Room names are required to be unique.

2.4.3. Public Rooms

The system shall provide a catalog of public chat rooms showing:

room name
description
current number of members

The catalog shall support simple search. Public rooms can be joined freely by any authenticated user unless banned.

2.4.4. Private Rooms

Private rooms are not visible in the public catalog. Users may join a private room only by invitation.

2.4.5. Joining and Leaving Rooms

Users may freely join public rooms unless banned from that room.
Users may leave rooms freely.
The owner cannot leave their own room.
The owner may only delete the room.

2.4.6. Room Deletion

If a chat room is deleted:

all messages in the room are deleted permanently
all files and images in the room are deleted permanently

2.4.7. Owner and Admin Roles

Each room has one owner.

Admins may:

delete messages in the room
remove members from the room
ban members from the room
view the list of banned users
view who banned each banned user
remove users from the ban list
remove admin status from other admins, except the owner

The owner may:

do all actions that an admin can do
remove any admin
remove any member
delete the room

2.4.8. Room Ban Rules

If a user is removed from a room by an admin, it is treated as a ban.

2.4.9. Room Invitations

Users may invite other users to private rooms.

2.5. Messaging

Users shall be able to send messages containing:

plain text
multiline text
emoji
attachments
reply/reference to another message

Maximum text size per message: 3 KB.

Messages shall be stored persistently and displayed in chronological order.

2.6. Attachments

Users shall be able to send:

images
arbitrary file types

Files shall be stored locally.

Maximum file size: 20 MB
Maximum image size: 3 MB

2.7. Notifications

Unread messages shall be indicated in the UI.

3. Non-Functional Requirements

Server supports up to 300 users.
Message delivery time: up to 3 seconds.
Presence updates: below 2 seconds.
History supports at least 10,000 messages.

4. UI Requirements

Typical chat layout:

top menu
message area
input at bottom
side panels

5. Notes and Clarifications

Usernames are unique and immutable.
Room names are unique.
Private rooms require invitation.
Public rooms are searchable.
Messages persist.
Files persist unless room is deleted.

Submission Guidelines

Attach a Word file with:

Your full name
Link to repository

Online Chat Server — Contest Requirements
