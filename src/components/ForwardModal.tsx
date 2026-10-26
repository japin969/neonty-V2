import React, { useState, useMemo } from 'react';
import {
  X,
  Search,
  Check,
  CornerUpRight,
  Send,
  Users,
  Radio,
  Bookmark,
  Volume2,
  FileText,
  Image as ImageIcon
} from 'lucide-react';
import { Chat, Message } from '../types';
import { TelegramAvatar } from './TelegramAvatar';

interface ForwardModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedMessages: Message[];
  sourceChat: Chat;
  allChats: Chat[];
  onConfirmForward: (targetChatIds: string[], commentText?: string) => Promise<void>;
}

export const ForwardModal: React.FC<ForwardModalProps> = ({
  isOpen,
  onClose,
  selectedMessages,
  sourceChat,
  allChats,
  onConfirmForward
}) => {
  const [selectedTargetIds, setSelectedTargetIds] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [commentText, setCommentText] = useState('');
  const [isSending, setIsSending] = useState(false);

  // Filter destination chats based on search query
  const filteredChats = useMemo(() => {
    return allChats.filter((c) => {
      if (!searchQuery.trim()) return true;
      const q = searchQuery.toLowerCase();
      return (
        c.title.toLowerCase().includes(q) ||
        (c.lastMessage?.text && c.lastMessage.text.toLowerCase().includes(q))
      );
    });
  }, [allChats, searchQuery]);

  if (!isOpen) return null;

  const toggleTargetChat = (chatId: string) => {
    setSelectedTargetIds((prev) =>
      prev.includes(chatId) ? prev.filter((id) => id !== chatId) : [...prev, chatId]
    );
  };

  const handleSelectAllTargets = () => {
    if (selectedTargetIds.length === filteredChats.length) {
      setSelectedTargetIds([]);
    } else {
      setSelectedTargetIds(filteredChats.map((c) => c.chatId));
    }
  };

  const handleSend = async () => {
    if (selectedTargetIds.length === 0 || isSending) return;
    setIsSending(true);
    try {
      await onConfirmForward(selectedTargetIds, commentText.trim() || undefined);
      // Reset state and close
      setSelectedTargetIds([]);
      setCommentText('');
      setSearchQuery('');
      onClose();
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/70 backdrop-blur-xs animate-in fade-in duration-150">
      <div
        className="w-full max-w-md bg-[#17212b] border border-[#242f3d] rounded-2xl shadow-2xl flex flex-col overflow-hidden max-h-[90vh] text-slate-100"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="bg-[#242f3d] px-4 py-3 flex items-center justify-between border-b border-[#101419]">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-[#5288c1]/20 flex items-center justify-center text-[#6ab2f2]">
              <CornerUpRight className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-semibold text-white text-[15px]">Forward Messages</h3>
              <p className="text-xs text-slate-400">
                {selectedMessages.length} {selectedMessages.length === 1 ? 'message' : 'messages'} selected from{' '}
                <span className="text-[#6ab2f2]">{sourceChat.title}</span>
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Selected Messages Snippet Preview (Collapsible/Scrollable) */}
        <div className="p-3 bg-[#0e1621]/90 border-b border-[#242f3d] max-h-32 overflow-y-auto space-y-2">
          <div className="flex items-center justify-between text-[11px] text-slate-400 uppercase tracking-wider font-semibold">
            <span>Message Preview</span>
            <span>{selectedMessages.length} selected</span>
          </div>
          {selectedMessages.map((msg) => (
            <div
              key={msg.msgId}
              className="text-xs border-l-2 border-[#5288c1] bg-[#17212b]/80 pl-2.5 pr-2 py-1.5 rounded-r-lg"
            >
              <div className="text-[11px] text-[#6ab2f2] font-semibold flex items-center gap-1">
                {msg.senderId === 'me' ? 'You' : msg.forwardInfo?.senderName || sourceChat.title}
                {msg.forwardInfo && (
                  <span className="text-[10px] text-slate-400 font-normal">
                    (originally from {msg.forwardInfo.senderName})
                  </span>
                )}
              </div>
              <div className="text-slate-200 truncate flex items-center gap-1.5 mt-0.5">
                {msg.type === 'voice' && <Volume2 className="w-3.5 h-3.5 text-amber-400 shrink-0" />}
                {msg.type === 'file' && <FileText className="w-3.5 h-3.5 text-blue-400 shrink-0" />}
                {msg.type === 'image' && <ImageIcon className="w-3.5 h-3.5 text-emerald-400 shrink-0" />}
                <span>{msg.text || (msg.type === 'voice' ? 'Voice Message' : 'Media')}</span>
              </div>
            </div>
          ))}
        </div>

        {/* Search destination chats */}
        <div className="p-3 border-b border-[#242f3d]">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search destination chats or channels..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-[#0e1621] text-white text-xs sm:text-sm pl-9 pr-3 py-2 rounded-xl border border-slate-700/60 focus:border-[#5288c1] focus:outline-none placeholder-slate-500"
            />
          </div>

          {/* Quick Select All / Target Chips */}
          <div className="flex items-center justify-between mt-2 pt-1 text-xs">
            <span className="text-slate-400">
              Pick one or more recipients:
            </span>
            <button
              onClick={handleSelectAllTargets}
              className="text-[#6ab2f2] hover:underline font-medium cursor-pointer"
            >
              {selectedTargetIds.length === filteredChats.length && filteredChats.length > 0
                ? 'Deselect All'
                : 'Select All'}
            </button>
          </div>

          {/* Selected recipients pills */}
          {selectedTargetIds.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mt-2 max-h-16 overflow-y-auto pt-1">
              {selectedTargetIds.map((targetId) => {
                const targetChat = allChats.find((c) => c.chatId === targetId);
                if (!targetChat) return null;
                return (
                  <span
                    key={targetId}
                    className="inline-flex items-center gap-1 text-[11px] bg-[#5288c1]/30 text-[#6ab2f2] border border-[#5288c1]/50 px-2 py-0.5 rounded-full"
                  >
                    <span className="truncate max-w-[120px]">{targetChat.title}</span>
                    <button
                      onClick={() => toggleTargetChat(targetId)}
                      className="hover:text-white ml-0.5 cursor-pointer"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </span>
                );
              })}
            </div>
          )}
        </div>

        {/* Destination Chats List */}
        <div className="flex-1 overflow-y-auto divide-y divide-[#101419]/40 p-1 min-h-[180px] max-h-[260px]">
          {filteredChats.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-36 text-slate-500 text-xs">
              <p>No chats found matching &quot;{searchQuery}&quot;</p>
            </div>
          ) : (
            filteredChats.map((targetChat) => {
              const isSelected = selectedTargetIds.includes(targetChat.chatId);
              const isCurrentChat = targetChat.chatId === sourceChat.chatId;

              return (
                <div
                  key={targetChat.chatId}
                  onClick={() => toggleTargetChat(targetChat.chatId)}
                  className={`flex items-center justify-between p-2.5 rounded-xl cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-[#5288c1]/20 border border-[#5288c1]/40'
                      : 'hover:bg-[#242f3d]/60 border border-transparent'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="relative shrink-0">
                      <TelegramAvatar
                        name={targetChat.title}
                        avatarUrl={targetChat.avatarUrl}
                        size={38}
                        isOnline={targetChat.type === 'direct' && targetChat.title.includes('Pavel')}
                      />
                      {targetChat.chatId === 'c_saved' && (
                        <div className="absolute -bottom-1 -right-1 w-4 h-4 rounded-full bg-[#5288c1] flex items-center justify-center text-white">
                          <Bookmark className="w-2.5 h-2.5" />
                        </div>
                      )}
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-1.5">
                        <span className="text-sm font-semibold text-white truncate max-w-[160px] sm:max-w-[190px]">
                          {targetChat.title}
                        </span>
                        {isCurrentChat && (
                          <span className="text-[10px] bg-slate-700/60 text-slate-300 px-1.5 py-0.2 rounded-sm">
                            Current
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400 flex items-center gap-1">
                        {targetChat.type === 'group' ? (
                          <>
                            <Users className="w-3 h-3 text-slate-400" />
                            <span>{targetChat.memberUids.length || 4} members</span>
                          </>
                        ) : targetChat.type === 'channel' ? (
                          <>
                            <Radio className="w-3 h-3 text-slate-400" />
                            <span>Broadcast Channel</span>
                          </>
                        ) : targetChat.chatId === 'c_saved' ? (
                          <span className="text-[#6ab2f2]">Your cloud storage</span>
                        ) : (
                          <span>Direct Message</span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Checkbox indicator */}
                  <div
                    className={`w-5 h-5 rounded-full flex items-center justify-center transition-all ${
                      isSelected
                        ? 'bg-[#5288c1] text-white shadow-xs'
                        : 'border-2 border-slate-600 bg-transparent'
                    }`}
                  >
                    {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Optional forward comment / caption */}
        <div className="p-3 bg-[#0e1621] border-t border-[#242f3d]">
          <input
            type="text"
            placeholder="Add a comment / caption (optional)..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            className="w-full bg-[#17212b] text-white text-xs sm:text-sm px-3 py-2 rounded-xl border border-slate-700/60 focus:border-[#5288c1] focus:outline-none placeholder-slate-500"
          />
        </div>

        {/* Modal Actions */}
        <div className="bg-[#242f3d] px-4 py-3 flex items-center justify-between border-t border-[#101419]">
          <span className="text-xs text-slate-300">
            {selectedTargetIds.length === 0 ? (
              <span className="text-slate-400">Select at least 1 chat</span>
            ) : (
              <span className="text-emerald-400 font-medium">
                Ready to forward to {selectedTargetIds.length}{' '}
                {selectedTargetIds.length === 1 ? 'chat' : 'chats'}
              </span>
            )}
          </span>

          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              disabled={isSending}
              className="px-3 py-1.5 rounded-lg text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-700/50 cursor-pointer transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleSend}
              disabled={selectedTargetIds.length === 0 || isSending}
              className={`flex items-center gap-1.5 px-4 py-1.5 rounded-lg text-xs font-semibold shadow-md transition-all cursor-pointer ${
                selectedTargetIds.length > 0 && !isSending
                  ? 'bg-[#5288c1] hover:bg-[#4076ad] text-white'
                  : 'bg-slate-700 text-slate-400 cursor-not-allowed opacity-60'
              }`}
            >
              {isSending ? (
                <span>Forwarding...</span>
              ) : (
                <>
                  <Send className="w-3.5 h-3.5 -ml-0.5" />
                  <span>
                    Forward {selectedTargetIds.length > 0 ? `(${selectedTargetIds.length})` : ''}
                  </span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
