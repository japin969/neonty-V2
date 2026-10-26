import { Message, MessageType, User } from '../types';
import { fileDb } from './fileDatabase';

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

class FirebaseSimulatorService {
  private isOnlineNetwork: boolean = true;
  private currentUser: User | null = {
    uid: 'me',
    phoneNumber: '+14155550199',
    username: 'neont_engineer',
    firstName: 'Senior',
    lastName: 'Engineer',
    bio: 'Reverse engineering Telegram Android pixel-by-pixel',
    avatarUrl: '',
    isOnline: true,
    lastSeen: Date.now()
  };

  private authListeners: ((state: AuthState) => void)[] = [];
  private networkListeners: ((online: boolean) => void)[] = [];
  private typingListeners: Record<string, ((typers: string[]) => void)[]> = {};

  constructor() {
    // Restore or sync current user
    if (this.currentUser) {
      fileDb.upsertUser(this.currentUser);
    }
  }

  getIsNetworkOnline(): boolean {
    return this.isOnlineNetwork;
  }

  setNetworkOnline(online: boolean) {
    this.isOnlineNetwork = online;
    this.networkListeners.forEach((l) => l(online));
    if (online) {
      this.syncPendingMessages();
    }
  }

  onNetworkChange(cb: (online: boolean) => void) {
    this.networkListeners.push(cb);
    return () => {
      this.networkListeners = this.networkListeners.filter((l) => l !== cb);
    };
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }

  onAuthStateChanged(cb: (state: AuthState) => void) {
    this.authListeners.push(cb);
    cb({ user: this.currentUser, isAuthenticated: !!this.currentUser });
    return () => {
      this.authListeners = this.authListeners.filter((l) => l !== cb);
    };
  }

  private notifyAuth() {
    const state: AuthState = {
      user: this.currentUser,
      isAuthenticated: !!this.currentUser
    };
    this.authListeners.forEach((l) => l(state));
  }

  // Firebase Auth methods
  async registerWithEmail(email: string, pass: string, displayName: string, username: string): Promise<User> {
    const uid = 'u_' + Math.random().toString(36).substring(2, 9);
    const newUser: User = {
      uid,
      phoneNumber: email,
      username: username.replace('@', ''),
      firstName: displayName,
      lastName: '',
      bio: 'Hey there! I am using Neont.',
      avatarUrl: '',
      isOnline: true,
      lastSeen: Date.now()
    };
    this.currentUser = newUser;
    await fileDb.upsertUser(newUser);
    this.notifyAuth();
    return newUser;
  }

  async loginWithEmail(email: string, _pass: string): Promise<User> {
    // Check known users or make user
    const users = await fileDb.readUsers();
    let user = users.find((u) => u.phoneNumber === email || u.username === email);
    if (!user) {
      user = {
        uid: 'u_' + Math.random().toString(36).substring(2, 9),
        phoneNumber: email,
        username: email.split('@')[0],
        firstName: email.split('@')[0],
        lastName: '',
        bio: 'Using Neont Messenger',
        avatarUrl: '',
        isOnline: true,
        lastSeen: Date.now()
      };
      await fileDb.upsertUser(user);
    }
    this.currentUser = user;
    this.notifyAuth();
    return user;
  }

  logout() {
    this.currentUser = null;
    this.notifyAuth();
  }

  // Realtime Database & Hybrid Sync
  async sendMessage(chatId: string, text: string, type: MessageType = 'text', mediaUrl?: string): Promise<Message> {
    const senderId = this.currentUser ? this.currentUser.uid : 'me';
    const msgId = 'msg_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6);

    const pendingMsg: Message = {
      msgId,
      chatId,
      senderId,
      text,
      type,
      mediaUrl,
      status: this.isOnlineNetwork ? 'pending' : 'pending',
      timestamp: Date.now()
    };

    // Step 1: Optimistic append to local file-based JSON
    await fileDb.appendMessage(chatId, pendingMsg);

    // Update chat last message in chats.json
    const chats = await fileDb.readChats();
    const chatIdx = chats.findIndex((c) => c.chatId === chatId);
    if (chatIdx >= 0) {
      chats[chatIdx].lastMessage = {
        text,
        senderId,
        timestamp: Date.now(),
        status: 'pending',
        type
      };
      await fileDb.writeChats(chats);
    }

    // Step 2: Push to Firebase transport if online
    if (this.isOnlineNetwork) {
      setTimeout(async () => {
        // Mark as sent
        await fileDb.updateMessageStatus(chatId, msgId, 'sent');
        if (chatIdx >= 0) {
          chats[chatIdx].lastMessage!.status = 'sent';
          await fileDb.writeChats(chats);
        }

        // Simulate delivery & reading
        setTimeout(async () => {
          await fileDb.updateMessageStatus(chatId, msgId, 'read');
          if (chatIdx >= 0) {
            chats[chatIdx].lastMessage!.status = 'read';
            await fileDb.writeChats(chats);
          }

          // Trigger simulated reply from bots/contacts
          this.simulateContactReply(chatId, text);
        }, 1200);
      }, 450);
    }

    return pendingMsg;
  }

  async forwardMessages(
    targetChatIds: string[],
    messagesToForward: Message[],
    sourceChatTitle?: string,
    commentText?: string
  ): Promise<void> {
    const senderId = this.currentUser ? this.currentUser.uid : 'me';
    const users = await fileDb.readUsers();
    const chats = await fileDb.readChats();

    const resolveSenderName = (senderUid: string) => {
      if (senderUid === 'me' || (this.currentUser && senderUid === this.currentUser.uid)) {
        return this.currentUser ? `${this.currentUser.firstName} ${this.currentUser.lastName}`.trim() || this.currentUser.username : 'You';
      }
      const u = users.find((usr) => usr.uid === senderUid);
      if (u) return `${u.firstName} ${u.lastName}`.trim() || u.username;
      if (senderUid === 'u_pavel') return 'Pavel Durov';
      if (senderUid === 'u_elena') return 'Elena Rostova';
      if (senderUid === 'u_alex') return 'Alex Volkov';
      if (senderUid === 'u_neont_bot') return 'Neont Bot';
      return 'Unknown';
    };

    const sortedMessages = [...messagesToForward].sort((a, b) => a.timestamp - b.timestamp);

    for (const targetChatId of targetChatIds) {
      let lastForwardedMsg: Message | null = null;
      for (const msg of sortedMessages) {
        const originalSender = msg.forwardInfo?.senderName || resolveSenderName(msg.senderId);
        const fwdMsgId = 'fwd_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
        const fwdMsg: Message = {
          msgId: fwdMsgId,
          chatId: targetChatId,
          senderId,
          text: msg.text,
          type: msg.type,
          mediaUrl: msg.mediaUrl,
          mediaDurationSec: msg.mediaDurationSec,
          waveform: msg.waveform,
          status: this.isOnlineNetwork ? 'sent' : 'pending',
          timestamp: Date.now(),
          forwardInfo: {
            senderName: originalSender,
            originalChatId: msg.chatId,
            originalChatTitle: sourceChatTitle || 'Chat'
          }
        };
        await fileDb.appendMessage(targetChatId, fwdMsg);
        lastForwardedMsg = fwdMsg;
      }

      if (commentText && commentText.trim()) {
        const commentMsg: Message = {
          msgId: 'msg_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
          chatId: targetChatId,
          senderId,
          text: commentText.trim(),
          type: 'text',
          status: this.isOnlineNetwork ? 'sent' : 'pending',
          timestamp: Date.now() + 1
        };
        await fileDb.appendMessage(targetChatId, commentMsg);
        lastForwardedMsg = commentMsg;
      }

      const chatIdx = chats.findIndex((c) => c.chatId === targetChatId);
      if (chatIdx >= 0 && lastForwardedMsg) {
        chats[chatIdx].lastMessage = {
          text: lastForwardedMsg.forwardInfo
            ? `↪ Forwarded: ${lastForwardedMsg.text}`
            : lastForwardedMsg.text,
          senderId,
          timestamp: Date.now(),
          status: 'sent',
          type: lastForwardedMsg.type
        };
        chats[chatIdx].unreadCount = (chats[chatIdx].unreadCount || 0) + sortedMessages.length;
      }
    }
    await fileDb.writeChats(chats);
  }

  private async syncPendingMessages() {
    const chats = await fileDb.readChats();
    for (const chat of chats) {
      const msgs = await fileDb.readMessages(chat.chatId);
      for (const m of msgs) {
        if (m.status === 'pending') {
          await fileDb.updateMessageStatus(chat.chatId, m.msgId, 'sent');
        }
      }
    }
  }

  private async simulateContactReply(chatId: string, userText: string) {
    if (chatId === 'c_saved') return;

    let sender = 'u_pavel';
    let replyText = 'Got it! The file persistence cache handled the transmission.';

    if (chatId === 'c_elena') {
      sender = 'u_elena';
      replyText = 'Checked the layout in Jetpack Compose! Fluid ripple and rounded corners match Telegram 1:1.';
    } else if (chatId === 'c_android_core') {
      sender = 'u_alex';
      replyText = 'FileDatabaseManager mutex lock verified clean. No IO deadlocks observed.';
    }

    // Trigger typing state
    this.broadcastTyping(chatId, [sender]);

    setTimeout(async () => {
      this.broadcastTyping(chatId, []);
      const replyMsgId = 'msg_' + Date.now();
      const replyMsg: Message = {
        msgId: replyMsgId,
        chatId,
        senderId: sender,
        text: replyText,
        type: 'text',
        status: 'read',
        timestamp: Date.now()
      };

      await fileDb.appendMessage(chatId, replyMsg);
      const chats = await fileDb.readChats();
      const chatIdx = chats.findIndex((c) => c.chatId === chatId);
      if (chatIdx >= 0) {
        chats[chatIdx].lastMessage = {
          text: replyText,
          senderId: sender,
          timestamp: Date.now(),
          status: 'read',
          type: 'text'
        };
        chats[chatIdx].unreadCount += 1;
        await fileDb.writeChats(chats);
      }
    }, 2000);
  }

  // Typing indicators
  onTyping(chatId: string, cb: (typers: string[]) => void) {
    if (!this.typingListeners[chatId]) {
      this.typingListeners[chatId] = [];
    }
    this.typingListeners[chatId].push(cb);
    return () => {
      this.typingListeners[chatId] = this.typingListeners[chatId].filter((l) => l !== cb);
    };
  }

  broadcastTyping(chatId: string, typers: string[]) {
    if (this.typingListeners[chatId]) {
      this.typingListeners[chatId].forEach((l) => l(typers));
    }
  }
}

export const firebaseSim = new FirebaseSimulatorService();
