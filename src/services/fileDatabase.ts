import { AppSettings, CallLog, Chat, FileIOLog, Message, MessageStatus, Story, User } from '../types';

const STORAGE_PREFIX = 'neont_db:';

const INITIAL_USERS: User[] = [
  {
    uid: 'u_pavel',
    phoneNumber: '+14155552671',
    username: 'durov',
    firstName: 'Pavel',
    lastName: 'Durov',
    bio: 'Telegram founder & CEO',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80',
    isOnline: true,
    lastSeen: Date.now()
  },
  {
    uid: 'u_elena',
    phoneNumber: '+6281234567890',
    username: 'elenarostova',
    firstName: 'Elena',
    lastName: 'Rostova',
    bio: 'Android UI / UX Designer @ Neont',
    avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80',
    isOnline: true,
    lastSeen: Date.now()
  },
  {
    uid: 'u_alex',
    phoneNumber: '+19175550198',
    username: 'alexv',
    firstName: 'Alex',
    lastName: 'Volkov',
    bio: 'Kernel & C++ engineer',
    avatarUrl: '',
    isOnline: false,
    lastSeen: Date.now() - 3600000 * 4
  },
  {
    uid: 'u_neont_bot',
    phoneNumber: '',
    username: 'NeontAssistantBot',
    firstName: 'Neont',
    lastName: 'Official Bot',
    bio: 'Official Neont service notifications & security verification',
    avatarUrl: '',
    isOnline: true,
    lastSeen: Date.now()
  }
];

const INITIAL_CHATS: Chat[] = [
  {
    chatId: 'c_saved',
    type: 'direct',
    title: 'Saved Messages',
    avatarUrl: '',
    isPinned: true,
    isMuted: false,
    unreadCount: 0,
    memberUids: ['me'],
    lastMessage: {
      text: 'FileDatabaseManager.kt snippet & architecture diagram notes',
      senderId: 'me',
      timestamp: Date.now() - 1000 * 60 * 15,
      status: 'read',
      type: 'text'
    }
  },
  {
    chatId: 'c_pavel',
    type: 'direct',
    title: 'Pavel Durov',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80',
    isPinned: true,
    isMuted: false,
    unreadCount: 2,
    memberUids: ['me', 'u_pavel'],
    lastMessage: {
      text: 'The file-based JSON persistence ensures instant cold boot without SQLite lock contention.',
      senderId: 'u_pavel',
      timestamp: Date.now() - 1000 * 60 * 4,
      status: 'read',
      type: 'text'
    }
  },
  {
    chatId: 'c_elena',
    type: 'direct',
    title: 'Elena Rostova',
    avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80',
    isPinned: false,
    isMuted: false,
    unreadCount: 0,
    memberUids: ['me', 'u_elena'],
    lastMessage: {
      text: 'Voice message (0:14)',
      senderId: 'u_elena',
      timestamp: Date.now() - 1000 * 60 * 45,
      status: 'read',
      type: 'voice'
    }
  },
  {
    chatId: 'c_android_core',
    type: 'group',
    title: 'Neont Android Core Team',
    avatarUrl: '',
    isPinned: false,
    isMuted: true,
    unreadCount: 7,
    memberUids: ['me', 'u_pavel', 'u_elena', 'u_alex'],
    lastMessage: {
      text: 'Alex: Mutex per file prevents race conditions during high-volume childEventListener bursts.',
      senderId: 'u_alex',
      timestamp: Date.now() - 1000 * 60 * 90,
      status: 'read',
      type: 'text'
    }
  },
  {
    chatId: 'c_neont_news',
    type: 'channel',
    title: 'Neont News & Releases',
    avatarUrl: '',
    isPinned: false,
    isMuted: true,
    unreadCount: 0,
    memberUids: ['me'],
    lastMessage: {
      text: 'Neont v1.0 released: 100% Jetpack Compose UI with offline-first file storage.',
      senderId: 'u_neont_bot',
      timestamp: Date.now() - 1000 * 3600 * 12,
      status: 'read',
      type: 'text'
    }
  }
];

const INITIAL_MESSAGES: Record<string, Message[]> = {
  c_pavel: [
    {
      msgId: 'm1',
      chatId: 'c_pavel',
      senderId: 'u_pavel',
      text: 'Hi! Welcome to Neont. How is the reverse-engineered Android UI looking?',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 30,
      reactions: { '🔥': 2 }
    },
    {
      msgId: 'm2',
      chatId: 'c_pavel',
      senderId: 'me',
      text: 'Working seamlessly! We adopted the hybrid architecture: local internal JSON storage + Firebase Realtime sync.',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 20
    },
    {
      msgId: 'm3',
      chatId: 'c_pavel',
      senderId: 'u_pavel',
      text: 'Excellent. Make sure FileDatabaseManager handles disk writes with a debounce mechanism.',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 10
    },
    {
      msgId: 'm4',
      chatId: 'c_pavel',
      senderId: 'u_pavel',
      text: 'The file-based JSON persistence ensures instant cold boot without SQLite lock contention.',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 4
    }
  ],
  c_saved: [
    {
      msgId: 'ms1',
      chatId: 'c_saved',
      senderId: 'me',
      text: 'Note to self: FileDatabaseManager uses context.filesDir/neont_db/ directory.',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 3600 * 2
    },
    {
      msgId: 'ms2',
      chatId: 'c_saved',
      senderId: 'me',
      text: 'FileDatabaseManager.kt snippet & architecture diagram notes',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 15
    }
  ],
  c_elena: [
    {
      msgId: 'me1',
      chatId: 'c_elena',
      senderId: 'me',
      text: 'Elena, did you check the bubble margins and status tick styling?',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 60
    },
    {
      msgId: 'me2',
      chatId: 'c_elena',
      senderId: 'u_elena',
      text: 'Yes! Rounded corners: 16dp with 2dp tail on the corresponding bottom corner.',
      type: 'text',
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 50
    },
    {
      msgId: 'me3',
      chatId: 'c_elena',
      senderId: 'u_elena',
      text: 'Here is a quick voice note explaining the haptic feedback curve.',
      type: 'voice',
      mediaDurationSec: 14,
      waveform: [30, 45, 80, 60, 95, 40, 70, 85, 50, 65, 90, 75, 40, 60],
      status: 'read',
      timestamp: Date.now() - 1000 * 60 * 45
    }
  ]
};

const INITIAL_STORIES: Story[] = [
  {
    storyId: 's1',
    userId: 'u_pavel',
    userName: 'Pavel Durov',
    userAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80',
    mediaUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80',
    timestamp: Date.now() - 1000 * 3600 * 2,
    caption: 'Neont architecture deep-dive presentation',
    isViewed: false
  },
  {
    storyId: 's2',
    userId: 'u_elena',
    userName: 'Elena Rostova',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80',
    mediaUrl: 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=600&q=80',
    timestamp: Date.now() - 1000 * 3600 * 5,
    caption: 'Jetpack Compose preview animations demo',
    isViewed: false
  }
];

const INITIAL_CALLS: CallLog[] = [
  {
    callId: 'call_1',
    userId: 'u_pavel',
    userName: 'Pavel Durov',
    userAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80',
    type: 'incoming',
    isVideo: false,
    timestamp: Date.now() - 1000 * 3600 * 8,
    durationSec: 245
  },
  {
    callId: 'call_2',
    userId: 'u_elena',
    userName: 'Elena Rostova',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80',
    type: 'outgoing',
    isVideo: true,
    timestamp: Date.now() - 1000 * 3600 * 26,
    durationSec: 612
  }
];

const INITIAL_SETTINGS: AppSettings = {
  theme: 'dark',
  language: 'en',
  notificationsEnabled: true,
  vibrateEnabled: true,
  autoDownloadMedia: true,
  saveToGallery: false,
  cacheSizeMb: 24,
  lastBackupTimestamp: Date.now() - 1000 * 3600 * 24
};

export class EmulatedFileDatabaseManager {
  private logListeners: ((log: FileIOLog) => void)[] = [];
  private debounceTimers: Record<string, NodeJS.Timeout> = {};

  constructor() {
    this.seedIfEmpty();
  }

  private seedIfEmpty() {
    if (!localStorage.getItem(STORAGE_PREFIX + 'users.json')) {
      this.writeJsonDirect('users.json', INITIAL_USERS);
    }
    if (!localStorage.getItem(STORAGE_PREFIX + 'chats.json')) {
      this.writeJsonDirect('chats.json', INITIAL_CHATS);
    }
    if (!localStorage.getItem(STORAGE_PREFIX + 'stories.json')) {
      this.writeJsonDirect('stories.json', INITIAL_STORIES);
    }
    if (!localStorage.getItem(STORAGE_PREFIX + 'calls.json')) {
      this.writeJsonDirect('calls.json', INITIAL_CALLS);
    }
    if (!localStorage.getItem(STORAGE_PREFIX + 'settings.json')) {
      this.writeJsonDirect('settings.json', INITIAL_SETTINGS);
    }
    Object.entries(INITIAL_MESSAGES).forEach(([chatId, msgs]) => {
      const fileName = `messages_${chatId}.json`;
      if (!localStorage.getItem(STORAGE_PREFIX + fileName)) {
        this.writeJsonDirect(fileName, msgs);
      }
    });
  }

  public onLog(cb: (log: FileIOLog) => void) {
    this.logListeners.push(cb);
    return () => {
      this.logListeners = this.logListeners.filter((l) => l !== cb);
    };
  }

  private log(op: FileIOLog['operation'], fileName: string, detail: string, durationMs: number = 2) {
    const entry: FileIOLog = {
      id: Math.random().toString(36).substring(2, 9),
      timestamp: Date.now(),
      operation: op,
      fileName,
      detail,
      durationMs
    };
    this.logListeners.forEach((l) => l(entry));
  }

  private readJson<T>(fileName: string, fallback: T): T {
    const start = performance.now();
    try {
      const raw = localStorage.getItem(STORAGE_PREFIX + fileName);
      const parsed = raw ? JSON.parse(raw) : fallback;
      this.log('READ', fileName, `Retrieved ${Array.isArray(parsed) ? parsed.length + ' items' : 'object'}`, Math.round(performance.now() - start));
      return parsed;
    } catch {
      return fallback;
    }
  }

  private writeJsonDirect<T>(fileName: string, data: T) {
    localStorage.setItem(STORAGE_PREFIX + fileName, JSON.stringify(data, null, 2));
  }

  private writeJsonDebounced<T>(fileName: string, data: T, delayMs: number = 300) {
    if (this.debounceTimers[fileName]) {
      clearTimeout(this.debounceTimers[fileName]);
    }
    this.debounceTimers[fileName] = setTimeout(() => {
      const start = performance.now();
      this.writeJsonDirect(fileName, data);
      this.log('DEBOUNCE_FLUSH', fileName, `Flushed to context.filesDir/neont_db/${fileName}`, Math.round(performance.now() - start));
      delete this.debounceTimers[fileName];
    }, delayMs);
  }

  // Users
  async readUsers(): Promise<User[]> {
    return this.readJson('users.json', []);
  }

  async writeUsers(users: User[]) {
    this.writeJsonDebounced('users.json', users);
  }

  async upsertUser(user: User) {
    const current = await this.readUsers();
    const idx = current.findIndex((u) => u.uid === user.uid);
    if (idx >= 0) {
      current[idx] = user;
    } else {
      current.push(user);
    }
    this.writeJsonDebounced('users.json', current);
  }

  // Chats
  async readChats(): Promise<Chat[]> {
    return this.readJson('chats.json', []);
  }

  async writeChats(chats: Chat[]) {
    this.writeJsonDebounced('chats.json', chats);
  }

  async upsertChat(chat: Chat) {
    const current = await this.readChats();
    const idx = current.findIndex((c) => c.chatId === chat.chatId);
    if (idx >= 0) {
      current[idx] = chat;
    } else {
      current.unshift(chat);
    }
    this.writeJsonDebounced('chats.json', current);
  }

  async deleteChat(chatId: string) {
    const current = await this.readChats();
    const filtered = current.filter((c) => c.chatId !== chatId);
    this.writeJsonDebounced('chats.json', filtered);
    localStorage.removeItem(STORAGE_PREFIX + `messages_${chatId}.json`);
    this.log('DELETE', `messages_${chatId}.json`, 'Deleted chat message file');
  }

  // Messages
  async readMessages(chatId: string): Promise<Message[]> {
    const fileName = `messages_${chatId}.json`;
    return this.readJson(fileName, []);
  }

  async writeMessages(chatId: string, messages: Message[]) {
    const fileName = `messages_${chatId}.json`;
    this.writeJsonDebounced(fileName, messages);
  }

  async appendMessage(chatId: string, message: Message) {
    const start = performance.now();
    const fileName = `messages_${chatId}.json`;
    const messages = await this.readMessages(chatId);
    const existingIdx = messages.findIndex((m) => m.msgId === message.msgId);
    if (existingIdx >= 0) {
      messages[existingIdx] = message;
    } else {
      messages.push(message);
    }
    this.writeJsonDirect(fileName, messages);
    this.log('APPEND', fileName, `msgId=${message.msgId.slice(0, 8)} status=${message.status}`, Math.round(performance.now() - start));
  }

  async updateMessageStatus(chatId: string, msgId: string, status: MessageStatus) {
    const fileName = `messages_${chatId}.json`;
    const messages = await this.readMessages(chatId);
    const idx = messages.findIndex((m) => m.msgId === msgId);
    if (idx >= 0) {
      messages[idx].status = status;
      this.writeJsonDirect(fileName, messages);
      this.log('WRITE', fileName, `msgId=${msgId.slice(0, 8)} updated to ${status}`);
    }
  }

  async deleteMessage(chatId: string, msgId: string) {
    const fileName = `messages_${chatId}.json`;
    const messages = await this.readMessages(chatId);
    const filtered = messages.filter((m) => m.msgId !== msgId);
    this.writeJsonDirect(fileName, filtered);
    this.log('DELETE', fileName, `Deleted msgId=${msgId.slice(0, 8)}`);
  }

  async deleteMultipleMessages(chatId: string, msgIds: string[]) {
    const fileName = `messages_${chatId}.json`;
    const messages = await this.readMessages(chatId);
    const idSet = new Set(msgIds);
    const filtered = messages.filter((m) => !idSet.has(m.msgId));
    this.writeJsonDirect(fileName, filtered);
    this.log('DELETE', fileName, `Batch deleted ${msgIds.length} messages`);
  }

  // Stories
  async readStories(): Promise<Story[]> {
    return this.readJson('stories.json', []);
  }

  async writeStories(stories: Story[]) {
    this.writeJsonDebounced('stories.json', stories);
  }

  // Calls
  async readCalls(): Promise<CallLog[]> {
    return this.readJson('calls.json', []);
  }

  async writeCalls(calls: CallLog[]) {
    this.writeJsonDebounced('calls.json', calls);
  }

  // Settings
  async readSettings(): Promise<AppSettings> {
    return this.readJson('settings.json', INITIAL_SETTINGS);
  }

  async writeSettings(settings: AppSettings) {
    this.writeJsonDebounced('settings.json', settings);
  }

  // Inspect all virtual files
  getAllVirtualFiles(): { fileName: string; sizeBytes: number; itemCount: number; raw: string }[] {
    const files: { fileName: string; sizeBytes: number; itemCount: number; raw: string }[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith(STORAGE_PREFIX)) {
        const fileName = key.replace(STORAGE_PREFIX, '');
        const raw = localStorage.getItem(key) || '';
        let count = 0;
        try {
          const parsed = JSON.parse(raw);
          count = Array.isArray(parsed) ? parsed.length : 1;
        } catch {
          count = 0;
        }
        files.push({
          fileName,
          sizeBytes: new Blob([raw]).size,
          itemCount: count,
          raw
        });
      }
    }
    return files.sort((a, b) => a.fileName.localeCompare(b.fileName));
  }
}

export const fileDb = new EmulatedFileDatabaseManager();
