export type MessageStatus = 'pending' | 'sent' | 'delivered' | 'read' | 'failed';

export type MessageType = 'text' | 'voice' | 'image' | 'sticker' | 'file';

export interface User {
  uid: string;
  phoneNumber: string;
  username: string;
  firstName: string;
  lastName: string;
  bio: string;
  avatarUrl: string;
  isOnline: boolean;
  lastSeen: number; // unix timestamp ms
}

export interface Message {
  msgId: string;
  chatId: string;
  senderId: string;
  text: string;
  type: MessageType;
  mediaUrl?: string;
  mediaDurationSec?: number;
  waveform?: number[];
  status: MessageStatus;
  timestamp: number;
  replyToMsgId?: string;
  reactions?: Record<string, number>;
  forwardInfo?: {
    senderName: string;
    originalChatId?: string;
    originalChatTitle?: string;
  };
}

export interface Chat {
  chatId: string;
  type: 'direct' | 'group' | 'channel';
  title: string;
  avatarUrl: string;
  isPinned: boolean;
  isMuted: boolean;
  unreadCount: number;
  lastMessage?: {
    text: string;
    senderId: string;
    timestamp: number;
    status: MessageStatus;
    type: MessageType;
  };
  memberUids: string[];
  typingUsers?: string[];
}

export interface Story {
  storyId: string;
  userId: string;
  userName: string;
  userAvatar: string;
  mediaUrl: string;
  timestamp: number;
  caption: string;
  isViewed: boolean;
}

export interface CallLog {
  callId: string;
  userId: string;
  userName: string;
  userAvatar: string;
  type: 'incoming' | 'outgoing' | 'missed';
  isVideo: boolean;
  timestamp: number;
  durationSec: number;
}

export interface AppSettings {
  theme: 'dark' | 'light' | 'amoled';
  language: string;
  notificationsEnabled: boolean;
  vibrateEnabled: boolean;
  autoDownloadMedia: boolean;
  saveToGallery: boolean;
  cacheSizeMb: number;
  lastBackupTimestamp: number;
}

export interface FileIOLog {
  id: string;
  timestamp: number;
  operation: 'READ' | 'WRITE' | 'APPEND' | 'DELETE' | 'DEBOUNCE_FLUSH';
  fileName: string;
  detail: string;
  durationMs: number;
}
