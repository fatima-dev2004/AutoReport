import React, { useState, useRef, useEffect } from 'react';
import { Send, Loader2, Sparkles, Trash2 } from 'lucide-react';
import Layout from '../components/Layout';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { chatWithGemini, getSuggestions } from '../api/gemini';

const initialAssistantMessage = {
  id: 1,
  role: 'assistant',
  content: '5. Les données seront validées et importées\n\nAssurez-vous que votre fichier respecte le format requis !',
  timestamp: new Date('2024-01-01T21:00:00'),
};

export default function AIChat() {
  const { user } = useAuth();
  const { t, language } = useLanguage();
  const [messages, setMessages] = useState([initialAssistantMessage]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;

    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const aiResponse = await chatWithGemini(input, language);

      const aiMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: aiResponse,
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch (err) {
      console.error('Erreur chat:', err);

      const errorMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: t('chat.error') || "Désolé, une erreur s'est produite. Veuillez réessayer.",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const clearChat = () => {
    setMessages([initialAssistantMessage]);
  };

  const quickSuggestions = getSuggestions(language);

  return (
    <Layout>
      <PageHeader
        eyebrow={t('aiChat.eyebrow')}
        title={t('aiChat.title')}
        action={
          <button onClick={clearChat} className="btn-secondary !px-4 !py-3 !rounded-xl whitespace-nowrap">
            <Trash2 size={16} />
            {t('aiChat.newConversation')}
          </button>
        }
      />

      <div className="flex min-h-[calc(100vh-170px)] flex-col px-0 pb-0 pt-0">
        <div className="flex-1 overflow-hidden border-b border-border bg-surface/30">
          <div className="chat-messages-container bg-transparent p-0 md:p-0">
            <div className="flex min-h-full flex-col justify-end gap-4 px-2 py-4 md:px-4">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex chat-message-enter ${
                    message.role === 'user' ? 'justify-end' : 'justify-start'
                  }`}
                >
                  <div
                    className={`max-w-[75%] rounded-2xl px-4 py-3 ${
                      message.role === 'user'
                        ? 'bg-[#dfeaf7] text-slate-900'
                        : 'bg-[#dfeaf4] text-slate-900'
                    }`}
                  >
                    <p className="whitespace-pre-wrap text-[16px] leading-relaxed font-medium">
                      {message.content}
                    </p>
                    <div className="mt-2 text-[10px] text-slate-500">
                      {message.timestamp.toLocaleTimeString('fr-FR', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </div>
                  </div>
                </div>
              ))}

              {loading && (
                <div className="flex justify-start chat-message-enter">
                  <div className="max-w-[75%] rounded-2xl bg-[#dfeaf4] px-4 py-3 text-slate-900">
                    <div className="flex items-center gap-2">
                      <Loader2 size={16} className="animate-spin text-blue-600" />
                      <span className="text-sm text-slate-700">L'IA réfléchit...</span>
                    </div>
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>
          </div>
        </div>

        <div className="bg-transparent px-3 pb-5 pt-4 md:px-6">
          <form onSubmit={handleSubmit} className="flex items-center gap-3 rounded-2xl border border-border bg-[#dfeaf4] px-3 py-2 shadow-sm">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Posez votre question ici..."
              className="flex-1 border-0 bg-transparent text-base text-slate-700 placeholder:text-slate-500 focus:outline-none focus:ring-0"
              disabled={loading}
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#b9cce6] text-slate-700 shadow-sm transition hover:bg-[#a8bfdc] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? (
                <Loader2 size={18} className="animate-spin" />
              ) : (
                <Send size={18} />
              )}
            </button>
          </form>

          <p className="mt-4 flex items-center gap-1 text-sm text-slate-600">
            <Sparkles size={13} className="text-slate-600" />
            Posez vos questions sur la génération de rapports, l'import Excel, les KPIs, etc.
          </p>
        </div>

        <div className="mt-2 grid grid-cols-1 gap-3 px-3 pb-4 md:grid-cols-3 md:px-6">
          {quickSuggestions.map((suggestion) => (
            <button
              key={suggestion}
              onClick={() => setInput(suggestion)}
              className="rounded-xl border border-border bg-[#eef3f8] px-4 py-3 text-left text-[15px] font-medium text-slate-800 shadow-sm transition hover:border-slate-300 hover:bg-[#e4edf7] disabled:opacity-60"
              disabled={loading}
            >
              {suggestion}
            </button>
          ))}
        </div>
      </div>
    </Layout>
  );
}
