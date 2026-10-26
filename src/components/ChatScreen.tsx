import React, { useState, useRef, useEffect } from 'react';
import {
  ArrowLeft,
  Phone,
  MoreVertical,
  Paperclip,
  Smile,
  Send,
  Mic,
  Check,
  CheckCheck,
  Clock,
  Play,
  Pause,
  Image as ImageIcon,
  FileText,
  MapPin,
  BarChart2,
  X,
  CornerUpRight,
  Trash2,
  CheckSquare,
  Share2
} from 'lucide-react';
import { Chat, Message, User } from '../types';
import { TelegramAvatar } from './TelegramAvatar';
import { ForwardModal } from './ForwardModal';
import { firebaseSim } from '../services/firebaseSimulator';
import { fileDb } from '../services/fileDatabase';

interface ChatScreenProps {
  chat: Chat;
  messages: Message[];
  currentUser: User | null;
  isTyping: boolean;
  allChats?: Chat[];
  onBack: () => void;
  onSendMessage: (text: string, type?: 'text' | 'voice', mediaUrl?: string) => void;
  onMessagesUpdated?: () => Promise<void> | void;
  onDeleteMessages?: (msgIds: string[]) => Promise<void> | void;
}

export const ChatScreen: React.FC<ChatScreenProps> = ({
  chat,
  messages,
  currentUser,
  isTyping,
  allChats = [],
  onBack,
  onSendMessage,
  onMessagesUpdated,
  onDeleteMessages
}) => {
  const [inputText, setInputText] = useState('');
  const [showAttachMenu, setShowAttachMenu] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [isRecordingVoice, setIsRecordingVoice] = useState(false);
  const [voiceSeconds, setVoiceSeconds] = useState(0);
  const [playingVoiceId, setPlayingVoiceId] = useState<string | null>(null);

  // Multi-message selection & forwarding state
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [selectedMsgIds, setSelectedMsgIds] = useState<string[]>([]);
  const [showForwardModal, setShowForwardModal] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isSelectionMode) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isTyping, isSelectionMode]);

  // Voice recording timer
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (isRecordingVoice) {
      timer = setInterval(() => {
        setVoiceSeconds((s) => s + 1);
      }, 1000);
    } else {
      setVoiceSeconds(0);
    }
    return () => clearInterval(timer);
  }, [isRecordingVoice]);

  // Auto-dismiss toast
  useEffect(() => {
    if (toastMessage) {
      const timer = setTimeout(() => {
        setToastMessage(null);
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [toastMessage]);

  const handleSend = () => {
    if (!inputText.trim()) return;
    onSendMessage(inputText.trim(), 'text');
    setInputText('');
  };

  const handleFinishVoiceRecord = () => {
    setIsRecordingVoice(false);
    onSendMessage(`Voice message (0:${voiceSeconds < 10 ? '0' : ''}${voiceSeconds})`, 'voice');
  };

  const handleQuickReaction = (emoji: string) => {
    onSendMessage(emoji, 'text');
    setShowEmojiPicker(false);
  };

  // --- Selection Mode Logic ---
  const toggleSelectMessage = (msgId: string) => {
    setSelectedMsgIds((prev) =>
      prev.includes(msgId) ? prev.filter((id) => id !== msgId) : [...prev, msgId]
    );
  };

  const handleStartSelection = (initialMsgId?: string) => {
    setIsSelectionMode(true);
    setShowMoreMenu(false);
    setSelectedMsgIds(initialMsgId ? [initialMsgId] : []);
  };

  const handleExitSelection = () => {
    setIsSelectionMode(false);
    setSelectedMsgIds([]);
  };

  const handleSelectAll = () => {
    if (selectedMsgIds.length === messages.length) {
      setSelectedMsgIds([]);
    } else {
      setSelectedMsgIds(messages.map((m) => m.msgId));
    }
  };

  const handleOpenForwardModal = () => {
    if (selectedMsgIds.length === 0) return;
    setShowForwardModal(true);
  };

  const handleConfirmForward = async (targetChatIds: string[], commentText?: string) => {
    const selectedMsgs = messages.filter((m) => selectedMsgIds.includes(m.msgId));
    if (selectedMsgs.length === 0) return;

    await firebaseSim.forwardMessages(targetChatIds, selectedMsgs, chat.title, commentText);

    // Refresh messages and chat lists
    if (onMessagesUpdated) {
      await onMessagesUpdated();
    }

    const countText = `${selectedMsgs.length} ${selectedMsgs.length === 1 ? 'message' : 'messages'}`;
    const targetCountText = `${targetChatIds.length} ${targetChatIds.length === 1 ? 'chat' : 'chats'}`;
    setToastMessage(`Forwarded ${countText} to ${targetCountText}`);

    // Exit selection mode
    handleExitSelection();
  };

  const handleDeleteSelected = async () => {
    if (selectedMsgIds.length === 0) return;
    const confirmDelete = window.confirm(
      `Delete ${selectedMsgIds.length} selected message(s)?`
    );
    if (!confirmDelete) return;

    if (onDeleteMessages) {
      await onDeleteMessages(selectedMsgIds);
    } else {
      await fileDb.deleteMultipleMessages(chat.chatId, selectedMsgIds);
      if (onMessagesUpdated) {
        await onMessagesUpdated();
      }
    }

    setToastMessage(`Deleted ${selectedMsgIds.length} message(s)`);
    handleExitSelection();
  };

  const selectedMessages = messages.filter((m) => selectedMsgIds.includes(m.msgId));

  return (
    <div className="flex flex-col h-full bg-[#0e1621] relative select-none">
      {/* Toast Notification Banner */}
      {toastMessage && (
        <div className="absolute top-16 left-1/2 -translate-x-1/2 z-40 bg-[#5288c1] text-white text-xs sm:text-sm font-medium px-4 py-2 rounded-full shadow-2xl flex items-center gap-2 animate-in fade-in slide-in-from-top-3 duration-200">
          <Check className="w-4 h-4 text-white stroke-[3]" />
          <span>{toastMessage}</span>
          <button
            onClick={() => setToastMessage(null)}
            className="hover:text-white/80 ml-1 cursor-pointer"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* TOP HEADER: Dynamic between Normal Header and Selection Action Bar */}
      {isSelectionMode ? (
        <div className="h-14 bg-[#2b5278] border-b border-[#101419] px-3 flex items-center justify-between z-20 shadow-md animate-in slide-in-from-top-1 duration-150">
          <div className="flex items-center gap-3">
            <button
              onClick={handleExitSelection}
              className="p-2 -ml-1 text-slate-200 hover:text-white hover:bg-white/10 rounded-full cursor-pointer transition-colors"
              title="Cancel Selection (Esc)"
            >
              <X className="w-5 h-5" />
            </button>
            <div className="flex items-center gap-2">
              <span className="text-[17px] font-bold text-white tracking-wide">
                {selectedMsgIds.length}
              </span>
              <span className="text-xs text-slate-200 hidden sm:inline">
                {selectedMsgIds.length === 1 ? 'message selected' : 'messages selected'}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-1.5 sm:gap-2">
            <button
              onClick={handleSelectAll}
              className="px-2.5 py-1 text-xs font-medium rounded-lg bg-white/15 hover:bg-white/25 text-white cursor-pointer transition-colors"
            >
              {selectedMsgIds.length === messages.length && messages.length > 0
                ? 'Deselect All'
                : 'Select All'}
            </button>

            <button
              onClick={handleOpenForwardModal}
              disabled={selectedMsgIds.length === 0}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold shadow-sm cursor-pointer transition-all ${
                selectedMsgIds.length > 0
                  ? 'bg-white text-[#2b5278] hover:bg-slate-100'
                  : 'bg-white/20 text-slate-300/60 cursor-not-allowed opacity-50'
              }`}
              title="Forward to other chats"
            >
              <CornerUpRight className="w-4 h-4 stroke-[2.5]" />
              <span>Forward</span>
              {selectedMsgIds.length > 0 && (
                <span className="bg-[#2b5278] text-white px-1.5 py-0.2 rounded-full text-[10px]">
                  {selectedMsgIds.length}
                </span>
              )}
            </button>

            <button
              onClick={handleDeleteSelected}
              disabled={selectedMsgIds.length === 0}
              className={`p-2 rounded-lg text-slate-200 hover:text-red-300 hover:bg-red-500/20 cursor-pointer transition-colors ${
                selectedMsgIds.length === 0 ? 'opacity-40 cursor-not-allowed' : ''
              }`}
              title="Delete selected messages"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      ) : (
        /* Normal Top Action Bar */
        <div className="h-14 bg-[#242f3d] border-b border-[#101419] px-2 flex items-center justify-between z-20 shadow-xs">
          <div className="flex items-center gap-2">
            <button
              onClick={onBack}
              className="p-2 -ml-1 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <TelegramAvatar
              name={chat.title}
              avatarUrl={chat.avatarUrl}
              size={40}
              isOnline={chat.type === 'direct' && chat.title.includes('Pavel')}
            />
            <div className="leading-tight ml-1">
              <h2 className="text-[15px] font-semibold text-white truncate max-w-[170px] sm:max-w-xs">
                {chat.title}
              </h2>
              <p className="text-[12px] text-slate-400">
                {isTyping ? (
                  <span className="text-[#4fa0ff] font-medium">typing...</span>
                ) : chat.type === 'group' ? (
                  `${chat.memberUids.length || 4} members`
                ) : chat.title.includes('Pavel') ? (
                  <span className="text-[#4fa0ff]">online</span>
                ) : (
                  'last seen recently'
                )}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-1 relative">
            {/* Quick multi-select launcher button */}
            <button
              onClick={() => handleStartSelection()}
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
              title="Select & Forward Messages"
            >
              <CheckSquare className="w-5 h-5 text-slate-300 hover:text-[#6ab2f2]" />
            </button>

            <button
              onClick={() => alert(`Initiating secure encrypted call with ${chat.title}`)}
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer"
              title="Voice Call"
            >
              <Phone className="w-5 h-5" />
            </button>

            <button
              onClick={() => setShowMoreMenu(!showMoreMenu)}
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer"
              title="Chat Options"
            >
              <MoreVertical className="w-5 h-5" />
            </button>

            {/* Dropdown menu */}
            {showMoreMenu && (
              <div className="absolute right-0 top-12 w-48 bg-[#242f3d] border border-slate-700/70 rounded-xl shadow-2xl py-1 z-30 animate-in fade-in duration-100">
                <button
                  onClick={() => handleStartSelection()}
                  className="w-full px-3.5 py-2 text-left text-xs sm:text-sm text-slate-200 hover:bg-[#17212b] flex items-center gap-2.5 cursor-pointer"
                >
                  <CheckSquare className="w-4 h-4 text-[#6ab2f2]" />
                  <span>Select Messages</span>
                </button>
                <button
                  onClick={() => {
                    setShowMoreMenu(false);
                    alert(`Chat info for ${chat.title}`);
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs sm:text-sm text-slate-200 hover:bg-[#17212b] flex items-center gap-2.5 cursor-pointer"
                >
                  <Share2 className="w-4 h-4 text-emerald-400" />
                  <span>Share Chat Link</span>
                </button>
                <button
                  onClick={() => {
                    setShowMoreMenu(false);
                    alert('Mute notifications toggled');
                  }}
                  className="w-full px-3.5 py-2 text-left text-xs sm:text-sm text-slate-200 hover:bg-[#17212b] flex items-center gap-2.5 cursor-pointer"
                >
                  <Clock className="w-4 h-4 text-amber-400" />
                  <span>Mute Notifications</span>
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Telegram Pattern Wallpaper & Message Stream */}
      <div
        className="flex-1 overflow-y-auto px-4 py-3 space-y-2.5 relative"
        style={{
          backgroundImage: `radial-gradient(circle at 50% 50%, rgba(36, 47, 61, 0.4) 1px, transparent 1px)`,
          backgroundSize: '24px 24px'
        }}
      >
        {/* Date bubble */}
        <div className="flex justify-center my-2">
          <span className="px-3 py-0.5 rounded-full bg-black/40 text-[11px] font-medium text-slate-300 backdrop-blur-xs">
            Today
          </span>
        </div>

        {/* Messages */}
        {messages.map((msg) => {
          const isMe = msg.senderId === 'me' || (currentUser && msg.senderId === currentUser.uid);
          const isSelected = selectedMsgIds.includes(msg.msgId);
          const time = new Date(msg.timestamp).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          });

          return (
            <div
              key={msg.msgId}
              onClick={() => {
                if (isSelectionMode) {
                  toggleSelectMessage(msg.msgId);
                }
              }}
              className={`group flex items-end gap-2 transition-all duration-150 relative py-0.5 rounded-xl ${
                isMe ? 'justify-end' : 'justify-start'
              } ${isSelectionMode ? 'cursor-pointer' : ''} ${
                isSelected ? 'bg-[#5288c1]/15 -mx-2 px-2' : ''
              }`}
            >
              {/* Checkbox indicator when in selection mode */}
              {isSelectionMode && (
                <div
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleSelectMessage(msg.msgId);
                  }}
                  className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-[#5288c1] text-white ring-2 ring-[#5288c1]/60 shadow-sm'
                      : 'border-2 border-slate-500 hover:border-[#6ab2f2] bg-slate-900/60'
                  } ${isMe ? 'order-last ml-1' : 'order-first mr-1'}`}
                >
                  {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                </div>
              )}

              {/* Message Bubble Container */}
              <div
                className={`relative max-w-[85%] sm:max-w-md px-3.5 py-2 rounded-2xl shadow-sm text-white transition-all ${
                  isMe
                    ? 'bg-[#2b5278] rounded-br-[4px]'
                    : 'bg-[#182533] rounded-bl-[4px]'
                } ${
                  isSelected
                    ? 'ring-2 ring-[#5288c1] ring-offset-2 ring-offset-[#0e1621] shadow-lg shadow-[#5288c1]/25'
                    : ''
                }`}
              >
                {/* Quick select/forward hover action when NOT in selection mode */}
                {!isSelectionMode && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleStartSelection(msg.msgId);
                    }}
                    className="opacity-0 group-hover:opacity-100 absolute -top-3 right-2 bg-[#242f3d] hover:bg-[#5288c1] text-slate-300 hover:text-white border border-slate-700/80 rounded-full px-2 py-0.5 text-[10px] font-medium shadow-md transition-all flex items-center gap-1 cursor-pointer z-10"
                    title="Select this message"
                  >
                    <CornerUpRight className="w-3 h-3" />
                    <span>Select</span>
                  </button>
                )}

                {/* Forwarded Header (Telegram Signature Style) */}
                {msg.forwardInfo && (
                  <div className="flex items-center gap-1 text-[11.5px] text-[#6ab2f2] font-semibold mb-1 pb-1 border-b border-white/10">
                    <CornerUpRight className="w-3.5 h-3.5 shrink-0 text-[#6ab2f2]" />
                    <span className="truncate">
                      Forwarded from{' '}
                      <span className="text-white hover:underline cursor-pointer">
                        {msg.forwardInfo.senderName}
                      </span>
                      {msg.forwardInfo.originalChatTitle && (
                        <span className="text-slate-400 font-normal ml-1">
                          ({msg.forwardInfo.originalChatTitle})
                        </span>
                      )}
                    </span>
                  </div>
                )}

                {/* Voice Note View */}
                {msg.type === 'voice' ? (
                  <div className="flex items-center gap-3 py-1">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setPlayingVoiceId(playingVoiceId === msg.msgId ? null : msg.msgId);
                      }}
                      className="w-10 h-10 rounded-full bg-[#5288c1] flex items-center justify-center text-white shrink-0 hover:bg-[#4076ad] cursor-pointer"
                    >
                      {playingVoiceId === msg.msgId ? (
                        <Pause className="w-5 h-5 fill-current" />
                      ) : (
                        <Play className="w-5 h-5 fill-current ml-0.5" />
                      )}
                    </button>
                    <div className="flex-1">
                      <div className="flex items-center gap-[3px] h-6">
                        {[40, 70, 95, 30, 60, 85, 45, 90, 65, 35, 75, 50, 80, 40].map((h, i) => (
                          <div
                            key={i}
                            className={`w-1 rounded-full transition-all ${
                              playingVoiceId === msg.msgId ? 'bg-white' : 'bg-slate-400'
                            }`}
                            style={{ height: `${h}%` }}
                          />
                        ))}
                      </div>
                      <span className="text-[11px] text-slate-300">
                        {msg.mediaDurationSec ? `0:${msg.mediaDurationSec}` : '0:14'}
                      </span>
                    </div>
                  </div>
                ) : (
                  /* Standard Text */
                  <p className="text-[14.5px] leading-relaxed whitespace-pre-wrap break-words pr-14">
                    {msg.text}
                  </p>
                )}

                {/* Footer Time & Status Ticks */}
                <div className="absolute right-2.5 bottom-1 flex items-center gap-1 text-[11px] text-slate-300/80">
                  <span>{time}</span>
                  {isMe && (
                    <span>
                      {msg.status === 'pending' && <Clock className="w-3 h-3 text-slate-300" />}
                      {msg.status === 'sent' && <Check className="w-3.5 h-3.5 text-[#6ab2f2]" />}
                      {(msg.status === 'delivered' || msg.status === 'read') && (
                        <CheckCheck className="w-3.5 h-3.5 text-[#6ab2f2]" />
                      )}
                    </span>
                  )}
                </div>

                {/* Reactions */}
                {msg.reactions && (
                  <div className="absolute -bottom-2.5 right-4 flex gap-1 bg-[#17212b] border border-slate-700/60 rounded-full px-2 py-0.5 text-[11px] shadow-sm">
                    {Object.entries(msg.reactions).map(([reaction, count]) => (
                      <span key={reaction}>
                        {reaction} {count > 1 ? count : ''}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* Typing indicator bubble */}
        {isTyping && (
          <div className="flex items-center gap-2 p-2 rounded-xl bg-[#182533] text-slate-300 text-xs w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-[#4fa0ff] animate-ping" />
            <span>{chat.title} is typing...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Attachment Sheet Modal */}
      {showAttachMenu && (
        <div className="absolute bottom-16 inset-x-4 bg-[#242f3d] rounded-2xl p-4 shadow-2xl border border-slate-700/60 z-30 animate-in slide-in-from-bottom duration-150">
          <div className="flex items-center justify-between pb-3 border-b border-slate-700/40 mb-3">
            <span className="text-sm font-semibold text-white">Share Content</span>
            <button
              onClick={() => setShowAttachMenu(false)}
              className="p-1 text-slate-400 hover:text-white cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
          <div className="grid grid-cols-4 gap-3 text-center">
            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('Photo: jetpack_compose_preview.png', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-blue-600/30 text-blue-400 flex items-center justify-center">
                <ImageIcon className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Gallery</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('Document: FileDatabaseManager.kt (3.4 KB)', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-emerald-600/30 text-emerald-400 flex items-center justify-center">
                <FileText className="w-6 h-6" />
              </div>
              <span className="text-[12px]">File</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('📍 Shared Location (San Francisco, CA)', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-amber-600/30 text-amber-400 flex items-center justify-center">
                <MapPin className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Location</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('📊 Poll: Should we use Gson or kotlinx.serialization for Neont?', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-purple-600/30 text-purple-400 flex items-center justify-center">
                <BarChart2 className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Poll</span>
            </button>
          </div>
        </div>
      )}

      {/* Quick Emoji / Sticker Panel */}
      {showEmojiPicker && (
        <div className="absolute bottom-16 left-4 bg-[#242f3d] rounded-2xl p-3 shadow-2xl border border-slate-700/60 z-30 flex gap-2">
          {['👍', '🔥', '❤️', '👏', '🚀', '⚡', '💯', '✨'].map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleQuickReaction(emoji)}
              className="text-2xl p-1.5 hover:scale-125 transition-transform cursor-pointer"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* BOTTOM BAR: Selection Action Drawer OR Standard Composer */}
      {isSelectionMode ? (
        <div className="p-3 bg-[#17212b] border-t border-[#101419] flex items-center justify-between">
          <div className="text-xs text-slate-300">
            {selectedMsgIds.length === 0 ? (
              <span>Tap messages above to select</span>
            ) : (
              <span className="text-white font-medium">
                {selectedMsgIds.length} {selectedMsgIds.length === 1 ? 'message' : 'messages'} chosen
              </span>
            )}
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleExitSelection}
              className="px-3 py-1.5 rounded-lg text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-700/50 cursor-pointer transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleOpenForwardModal}
              disabled={selectedMsgIds.length === 0}
              className={`flex items-center gap-1.5 px-4 py-1.5 rounded-lg text-xs font-semibold shadow-md transition-all cursor-pointer ${
                selectedMsgIds.length > 0
                  ? 'bg-[#5288c1] hover:bg-[#4076ad] text-white'
                  : 'bg-slate-700 text-slate-400 cursor-not-allowed opacity-50'
              }`}
            >
              <CornerUpRight className="w-3.5 h-3.5" />
              <span>Forward to...</span>
            </button>
          </div>
        </div>
      ) : (
        /* Standard Composer Bottom Bar */
        <div className="p-2.5 bg-[#17212b] border-t border-[#101419] flex items-center gap-2">
          {isRecordingVoice ? (
            /* Live Voice Recording UI */
            <div className="flex-1 flex items-center justify-between px-3 py-2 bg-red-950/40 border border-red-800/40 rounded-full animate-pulse">
              <div className="flex items-center gap-2 text-red-400 text-sm font-medium">
                <span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-ping" />
                <span>
                  Recording 0:{voiceSeconds < 10 ? '0' : ''}{voiceSeconds}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setIsRecordingVoice(false)}
                  className="text-xs text-slate-400 hover:text-white px-2 py-1 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={handleFinishVoiceRecord}
                  className="px-3 py-1 bg-red-600 text-white text-xs font-semibold rounded-full hover:bg-red-500 cursor-pointer"
                >
                  Send
                </button>
              </div>
            </div>
          ) : (
            /* Standard Composer */
            <>
              <button
                onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                className="p-2 text-slate-400 hover:text-[#5288c1] rounded-full cursor-pointer transition-colors"
                title="Emoji & Stickers"
              >
                <Smile className="w-6 h-6" />
              </button>

              <input
                type="text"
                placeholder="Message"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleSend();
                  }
                }}
                className="flex-1 bg-transparent text-white text-[15px] placeholder-slate-400 outline-none px-2"
              />

              <button
                onClick={() => setShowAttachMenu(!showAttachMenu)}
                className="p-2 text-slate-400 hover:text-white rounded-full cursor-pointer transition-colors"
                title="Attach Media or Documents"
              >
                <Paperclip className="w-5 h-5 -rotate-45" />
              </button>

              {inputText.trim() ? (
                <button
                  onClick={handleSend}
                  className="w-10 h-10 rounded-full bg-[#5288c1] hover:bg-[#4076ad] flex items-center justify-center text-white shadow-md cursor-pointer transition-transform active:scale-95"
                >
                  <Send className="w-5 h-5 -ml-0.5" />
                </button>
              ) : (
                <button
                  onClick={() => setIsRecordingVoice(true)}
                  className="w-10 h-10 rounded-full bg-[#242f3d] hover:bg-[#2e3b4d] text-slate-300 hover:text-[#5288c1] flex items-center justify-center cursor-pointer transition-colors"
                  title="Click to record voice note"
                >
                  <Mic className="w-5 h-5" />
                </button>
              )}
            </>
          )}
        </div>
      )}

      {/* Forward Modal */}
      <ForwardModal
        isOpen={showForwardModal}
        onClose={() => setShowForwardModal(false)}
        selectedMessages={selectedMessages}
        sourceChat={chat}
        allChats={allChats}
        onConfirmForward={handleConfirmForward}
      />
    </div>
  );
};
