# 🌍 Correction : Langue de l'Assistant IA

## 🐛 Problème

L'assistant IA répond **toujours en français**, même quand l'utilisateur a sélectionné **Anglais** dans l'interface.

**Cause** : La préférence de langue de l'utilisateur n'est **pas transmise** au backend/API IA.

---

## ✅ Solution : Transmettre la Langue au Backend IA

### Étape 1️⃣ : Frontend - Récupérer la Langue Actuelle

```javascript
// Dans votre composant de chat/assistant IA

// Récupérer la langue depuis le localStorage ou le contexte
const currentLanguage = localStorage.getItem('language') || 'fr';
// Résultat : 'fr' ou 'en'
```

### Étape 2️⃣ : Construire le Prompt Système Dynamique

```javascript
// Créer un prompt système qui change selon la langue
const systemPrompt = currentLanguage === 'en' 
  ? `You are a helpful assistant for a production reporting system. 
     Always respond in English. 
     Be concise, professional, and provide actionable guidance.`
  : `Tu es un assistant pour un système de reporting de production. 
     Réponds toujours en français. 
     Sois concis, professionnel et fournis des conseils pratiques.`;
```

### Étape 3️⃣ : Envoyer au Backend IA

#### Option A : API OpenAI / ChatGPT

```javascript
async function sendMessageToAI(userMessage) {
  const currentLanguage = localStorage.getItem('language') || 'fr';
  
  const systemPrompt = currentLanguage === 'en' 
    ? "Always respond in English. Be concise and professional."
    : "Réponds toujours en français. Sois concis et professionnel.";

  const response = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${OPENAI_API_KEY}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model: 'gpt-4',
      messages: [
        { 
          role: 'system', 
          content: systemPrompt // ✅ Langue dynamique
        },
        { 
          role: 'user', 
          content: userMessage 
        }
      ]
    })
  });

  const data = await response.json();
  return data.choices[0].message.content;
}
```

#### Option B : API Anthropic / Claude

```javascript
async function sendMessageToAI(userMessage) {
  const currentLanguage = localStorage.getItem('language') || 'fr';
  
  const systemPrompt = currentLanguage === 'en' 
    ? "Always respond in English."
    : "Réponds toujours en français.";

  const response = await fetch('https://api.anthropic.com/v1/messages', {
    method: 'POST',
    headers: {
      'x-api-key': ANTHROPIC_API_KEY,
      'anthropic-version': '2023-06-01',
      'content-type': 'application/json'
    },
    body: JSON.stringify({
      model: 'claude-3-sonnet-20240229',
      max_tokens: 1024,
      system: systemPrompt, // ✅ Langue dynamique
      messages: [
        { 
          role: 'user', 
          content: userMessage 
        }
      ]
    })
  });

  const data = await response.json();
  return data.content[0].text;
}
```

#### Option C : Backend Intermédiaire (Spring Boot)

**Frontend :**
```javascript
async function sendMessageToAI(userMessage) {
  const currentLanguage = localStorage.getItem('language') || 'fr';
  
  const response = await axios.post('/api/ai/chat', {
    message: userMessage,
    language: currentLanguage // ✅ Transmettre la langue
  }, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.data.response;
}
```

**Backend (nouveau controller) :**
```java
package com.sews.reporting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String language = request.getOrDefault("language", "fr");
        
        // Construire le prompt système
        String systemPrompt = language.equals("en")
            ? "Always respond in English. Be concise and professional."
            : "Réponds toujours en français. Sois concis et professionnel.";
        
        // TODO: Appeler votre service IA avec systemPrompt + message
        // String aiResponse = aiService.chat(systemPrompt, message);
        
        // Pour l'instant, retour mock
        String aiResponse = language.equals("en") 
            ? "I can help you with production reports, data import, and user management."
            : "Je peux vous aider avec les rapports de production, l'import de données et la gestion des utilisateurs.";
        
        return ResponseEntity.ok(Map.of(
            "response", aiResponse,
            "language", language
        ));
    }
}
```

---

## 📋 Checklist de Correction

- [ ] **Frontend** : Récupérer `currentLanguage` depuis localStorage/contexte
- [ ] **Frontend** : Créer `systemPrompt` dynamique (EN ou FR)
- [ ] **Frontend** : Transmettre `language` dans la requête API IA
- [ ] **Backend/API IA** : Utiliser `language` pour construire le prompt système
- [ ] **Tester** : Changer langue UI → vérifier réponse IA dans la bonne langue

---

## 🧪 Test

1. Ouvrir l'application
2. Sélectionner **Anglais** dans le sélecteur de langue
3. Poser une question à l'assistant IA
4. **Vérifier** : La réponse doit être en anglais ✅

5. Sélectionner **Français**
6. Poser une question
7. **Vérifier** : La réponse doit être en français ✅

---

## 📝 Exemple de Prompts Système

### Français
```
Tu es un assistant IA pour un système de reporting de production industrielle.

Ton rôle :
- Aider les utilisateurs à générer des rapports
- Expliquer comment importer des données Excel
- Guider dans la configuration des objectifs de production
- Répondre aux questions sur les statistiques

Style :
- Réponds TOUJOURS en français
- Sois concis et professionnel
- Fournis des étapes concrètes et actionnables
- Utilise un langage clair et technique
```

### English
```
You are an AI assistant for an industrial production reporting system.

Your role:
- Help users generate reports
- Explain how to import Excel data
- Guide users in setting production objectives
- Answer questions about statistics

Style:
- ALWAYS respond in English
- Be concise and professional
- Provide concrete and actionable steps
- Use clear and technical language
```

---

## 🎯 Résultat Attendu

Avant correction :
- UI en anglais ✅
- Réponse IA en français ❌

Après correction :
- UI en anglais ✅
- Réponse IA en anglais ✅

---

## 💡 Bonus : Questions Suggérées Multilingues

Si vous avez des **questions suggérées** ("Comment générer un rapport ?"), créez-les dynamiquement aussi :

```javascript
const suggestedQuestions = currentLanguage === 'en' ? [
  "How to generate a production report?",
  "How to import data from Excel?",
  "How to set production objectives?",
  "How to manage users and permissions?"
] : [
  "Comment générer un rapport de production ?",
  "Comment importer des données depuis Excel ?",
  "Comment définir des objectifs de production ?",
  "Comment gérer les utilisateurs et permissions ?"
];
```

---

**Fichier créé le :** 2026-08-10  
**Auteur :** Kiro AI Assistant
