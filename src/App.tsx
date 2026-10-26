import React, { useState, useEffect } from 'react';
import { TelegramHeader } from './components/TelegramHeader';
import { TelegramDrawer } from './components/TelegramDrawer';
import { StoryBar } from './components/StoryBar';
import { ChatListItem } from './components/ChatListItem';
import { ChatScreen } from './components/ChatScreen';
import { FileStorageInspector } from './components/FileStorageInspector';
import { AndroidCodeViewer } from './components/AndroidCodeViewer';
import { AuthModal } from './components/AuthModal';
import { fileDb } from './services/fileDatabase';
import { firebaseSim, AuthState } from './services/firebaseSimulator';
import { Chat, Message, Story, User } from './types';
import { HardDrive, FolderCode, Smartphone, Monitor } from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isStorageInspectorOpen, setIsStorageInspectorOpen] = useState(false);
  const [isCodeViewerOpen, setIsCodeViewerOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isMobileFrame, setIsMobileFrame] = useState(false);

  // Core App State
  const [currentUser, setCurrentUser] = useState<User | null>(firebaseSim.getCurrentUser());
  const [isOnlineNetwork, setIsOnlineNetwork] = useState<boolean>(firebaseSim.getIsNetworkOnline());
  const [chats, setChats] = useState<Chat[]>([]);
  const [stories, setStories] = useState<Story[]>([]);
  const [selectedChatId, setSelectedChatId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [activeTypers, setActiveTypers] = useState<string[]>([]);

  // Load initial data from local file database (cold-start simulation)
  useEffect(() => {
    const loadData = async () => {
      const c = await fileDb.readChats();
      const s = await fileDb.readStories();
      setChats(c);
      setStories(s);
    };
    loadData();

    // Listen to Auth State
    const unsubAuth = firebaseSim.onAuthStateChanged((state: AuthState) => {
      setCurrentUser(state.user);
    });

    // Listen to Network changes
    const unsubNet = firebaseSim.onNetworkChange((online: boolean) => {
      setIsOnlineNetwork(online);
    });

    return () => {
      unsubAuth();
      unsubNet();
    };
  }, []);

  // When selectedChatId changes, load messages from messages_{chatId}.json
  useEffect(() => {
    if (!selectedChatId) return;

    const loadMessages = async () => {
      const msgs = await fileDb.readMessages(selectedChatId);
      setMessages(msgs);
    };
    loadMessages();

    // Subscribe to typing indicators
    const unsubTyping = firebaseSim.onTyping(selectedChatId, (typers) => {
      setActiveTypers(typers);
    });

    return () => {
      unsubTyping();
    };
  }, [selectedChatId]);

  // Handle sending a message
  const handleSendMessage = async (text: string, type: 'text' | 'voice' = 'text', mediaUrl?: string) => {
    if (!selectedChatId) return;

    // Send through Firebase simulator with hybrid optimistic file storage
    await firebaseSim.sendMessage(selectedChatId, text, type, mediaUrl);

    // Refresh messages from local JSON
    const updatedMsgs = await fileDb.readMessages(selectedChatId);
    setMessages(updatedMsgs);

    // Refresh chats
    const updatedChats = await fileDb.readChats();
    setChats(updatedChats);
  };

  const handleRefreshChatData = async () => {
    if (selectedChatId) {
      const updatedMsgs = await fileDb.readMessages(selectedChatId);
      setMessages(updatedMsgs);
    }
    const updatedChats = await fileDb.readChats();
    setChats(updatedChats);
  };

  const handleDeleteMessages = async (msgIds: string[]) => {
    if (!selectedChatId) return;
    await fileDb.deleteMultipleMessages(selectedChatId, msgIds);
    await handleRefreshChatData();
  };

  // Filter chats by tab and search
  const filteredChats = chats.filter((chat) => {
    if (activeTab === 'personal' && chat.type !== 'direct') return false;
    if (activeTab === 'groups' && chat.type !== 'group') return false;
    if (activeTab === 'channels' && chat.type !== 'channel') return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = chat.title.toLowerCase().includes(q);
      const matchLastMsg = chat.lastMessage?.text.toLowerCase().includes(q);
      return matchTitle || matchLastMsg;
    }
    return true;
  });

  const selectedChat = chats.find((c) => c.chatId === selectedChatId);

  return (
    <div className="min-h-screen bg-[#0e1621] text-slate-100 flex flex-col font-sans">
      {/* Top Banner Toolbar for Developer & Preview Controls */}
      <div className="bg-[#17212b] border-b border-[#101419] px-4 py-2 flex items-center justify-between text-xs text-slate-300 z-40 select-none">
        <div className="flex items-center gap-2">
          <span className="font-bold text-white tracking-wide">Neont Messenger</span>
          <span className="hidden sm:inline text-slate-500">•</span>
          <span className="hidden sm:inline text-slate-400">
            Offline-First JSON Files + Firebase Realtime DB
          </span>
        </div>

        <div className="flex items-center gap-2">
          {/* Mobile frame toggle */}
          <button
            onClick={() => setIsMobileFrame(!isMobileFrame)}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-[#242f3d] hover:bg-[#2e3b4d] text-slate-200 cursor-pointer transition-colors"
            title="Toggle Android Device Frame"
          >
            {isMobileFrame ? <Monitor className="w-3.5 h-3.5" /> : <Smartphone className="w-3.5 h-3.5" />}
            <span className="hidden sm:inline">{isMobileFrame ? 'Desktop' : 'Phone Frame'}</span>
          </button>

          {/* Storage Inspector Button */}
          <button
            onClick={() => setIsStorageInspectorOpen(true)}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-[#5288c1]/20 hover:bg-[#5288c1]/30 text-[#6ab2f2] cursor-pointer transition-colors"
          >
            <HardDrive className="w-3.5 h-3.5" />
            <span>neont_db/</span>
          </button>

          {/* Android Kotlin Source Code */}
          <button
            onClick={() => setIsCodeViewerOpen(true)}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 cursor-pointer transition-colors"
          >
            <FolderCode className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Kotlin Source</span>
          </button>
        </div>
      </div>

      {/* Main Container */}
      <main className="flex-1 flex items-center justify-center p-0 sm:p-4 overflow-hidden">
        <div
          className={`w-full h-full flex flex-col bg-[#17212b] overflow-hidden transition-all duration-200 ${
            isMobileFrame
              ? 'max-w-[420px] max-h-[860px] rounded-3xl border-4 border-[#242f3d] shadow-2xl'
              : 'max-w-5xl h-[calc(100vh-45px)] sm:rounded-2xl border border-[#101419] shadow-xl'
          }`}
        >
          {selectedChatId && selectedChat ? (
            /* Active Chat Conversation Screen */
            <ChatScreen
              chat={selectedChat}
              messages={messages}
              currentUser={currentUser}
              isTyping={activeTypers.length > 0}
              allChats={chats}
              onBack={() => {
                setSelectedChatId(null);
                setActiveTypers([]);
              }}
              onSendMessage={handleSendMessage}
              onMessagesUpdated={handleRefreshChatData}
              onDeleteMessages={handleDeleteMessages}
            />
          ) : (
            /* Chat List & Feed Screen */
            <div className="flex flex-col h-full overflow-hidden">
              <TelegramHeader
                activeTab={activeTab}
                onTabChange={setActiveTab}
                onMenuClick={() => setIsDrawerOpen(true)}
                onSearchChange={setSearchQuery}
                onOpenStorageInspector={() => setIsStorageInspectorOpen(true)}
                onOpenCodeViewer={() => setIsCodeViewerOpen(true)}
                isOnlineNetwork={isOnlineNetwork}
                onToggleNetwork={() => firebaseSim.setNetworkOnline(!isOnlineNetwork)}
              />

              {/* Stories Bar */}
              <StoryBar
                stories={stories}
                onAddStory={() => alert('Camera / Media selector: Add to my Telegram Story')}
              />

              {/* Chat list */}
              <div className="flex-1 overflow-y-auto divide-y divide-[#101419]/40">
                {filteredChats.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-48 text-slate-400 text-sm">
                    <p>No chats found in this folder</p>
                  </div>
                ) : (
                  filteredChats.map((chat) => (
                    <ChatListItem
                      key={chat.chatId}
                      chat={chat}
                      isTyping={chat.chatId === selectedChatId && activeTypers.length > 0}
                      onClick={() => setSelectedChatId(chat.chatId)}
                    />
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Telegram Drawer */}
      <TelegramDrawer
        isOpen={isDrawerOpen}
        user={currentUser}
        isOnlineNetwork={isOnlineNetwork}
        onClose={() => setIsDrawerOpen(false)}
        onOpenSavedMessages={() => {
          setIsDrawerOpen(false);
          setSelectedChatId('c_saved');
        }}
        onOpenStorageInspector={() => {
          setIsDrawerOpen(false);
          setIsStorageInspectorOpen(true);
        }}
        onOpenCodeViewer={() => {
          setIsDrawerOpen(false);
          setIsCodeViewerOpen(true);
        }}
        onOpenAuthModal={() => {
          setIsDrawerOpen(false);
          setIsAuthModalOpen(true);
        }}
        onToggleNetwork={() => firebaseSim.setNetworkOnline(!isOnlineNetwork)}
      />

      {/* Modals */}
      <FileStorageInspector
        isOpen={isStorageInspectorOpen}
        onClose={() => setIsStorageInspectorOpen(false)}
      />

      <AndroidCodeViewer
        isOpen={isCodeViewerOpen}
        onClose={() => setIsCodeViewerOpen(false)}
      />

      <AuthModal
        isOpen={isAuthModalOpen}
        currentUser={currentUser}
        onClose={() => setIsAuthModalOpen(false)}
        onAuthSuccess={(user) => setCurrentUser(user)}
      />
    </div>
  );
}
