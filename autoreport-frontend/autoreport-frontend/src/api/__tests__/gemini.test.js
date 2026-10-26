import { describe, it, expect, vi, beforeEach } from 'vitest';
import { chatWithGemini, isGeminiConfigured, getSuggestions } from '../gemini';
import axios from 'axios';

vi.mock('axios');

describe('Gemini API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('isGeminiConfigured', () => {
    it('retourne false si la clé API n\'est pas configurée', () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', '');
      expect(isGeminiConfigured()).toBe(false);
    });

    it('retourne false si la clé API est la valeur par défaut', () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', 'your_gemini_api_key_here');
      expect(isGeminiConfigured()).toBe(false);
    });
  });

  describe('getSuggestions', () => {
    it('retourne une liste de suggestions', () => {
      const suggestions = getSuggestions();
      
      expect(Array.isArray(suggestions)).toBe(true);
      expect(suggestions.length).toBeGreaterThan(0);
      expect(suggestions[0]).toContain('rapport');
    });

    it('retourne des suggestions pertinentes pour AutoReport', () => {
      const suggestions = getSuggestions();
      
      const hasReportSuggestion = suggestions.some(s => 
        s.toLowerCase().includes('rapport')
      );
      const hasExcelSuggestion = suggestions.some(s => 
        s.toLowerCase().includes('excel')
      );
      
      expect(hasReportSuggestion).toBe(true);
      expect(hasExcelSuggestion).toBe(true);
    });
  });

  describe('chatWithGemini', () => {
    it('lance une erreur si la clé API n\'est pas configurée', async () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', '');
      
      const messages = [
        { role: 'user', content: 'Test message' }
      ];

      await expect(chatWithGemini(messages)).rejects.toThrow(
        /Clé API Gemini non configurée/
      );
    });

    it('envoie la requête correcte à l\'API Gemini', async () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', 'test-api-key-123');
      
      const mockResponse = {
        data: {
          candidates: [{
            content: {
              parts: [{ text: 'Test response from Gemini' }]
            }
          }]
        }
      };

      axios.post = vi.fn().mockResolvedValue(mockResponse);

      const messages = [
        { role: 'user', content: 'Comment générer un rapport ?' }
      ];

      const response = await chatWithGemini(messages);

      expect(response).toBe('Test response from Gemini');
      expect(axios.post).toHaveBeenCalledWith(
        expect.stringContaining('generativelanguage.googleapis.com'),
        expect.objectContaining({
          contents: expect.any(Array),
          generationConfig: expect.any(Object),
        }),
        expect.any(Object)
      );
    });

    it('gère les erreurs API correctement', async () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', 'test-api-key-123');
      
      axios.post = vi.fn().mockRejectedValue({
        response: {
          status: 403,
          data: { error: { message: 'API key invalid' } }
        }
      });

      const messages = [{ role: 'user', content: 'Test' }];

      await expect(chatWithGemini(messages)).rejects.toThrow(/Clé API invalide/);
    });

    it('transforme correctement les messages pour Gemini', async () => {
      vi.stubEnv('VITE_GEMINI_API_KEY', 'test-api-key-123');
      
      const mockResponse = {
        data: {
          candidates: [{
            content: { parts: [{ text: 'Response' }] }
          }]
        }
      };

      axios.post = vi.fn().mockResolvedValue(mockResponse);

      const messages = [
        { role: 'user', content: 'Question 1' },
        { role: 'assistant', content: 'Answer 1' },
        { role: 'user', content: 'Question 2' },
      ];

      await chatWithGemini(messages);

      const callArgs = axios.post.mock.calls[0][1];
      expect(callArgs.contents).toHaveLength(3);
      expect(callArgs.contents[0].role).toBe('user');
      expect(callArgs.contents[1].role).toBe('model'); // assistant -> model
      expect(callArgs.contents[2].role).toBe('user');
    });
  });
});
