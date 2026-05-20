// /js/adminchatjs.js

// Global WebSocket variable
let chatWebSocket = null;
const maxReconnectAttempts = 5;
let reconnectAttempts = 0;
let reconnectTimeout = null;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize chat when DOM is fully loaded
    initializeChat();

    // Setup WebSocket connection
    setupWebSocket();
});

function initializeChat() {
    // Load initial messages
    cargarChats();

    // Set up form submission
    const chatForm = document.getElementById('chat-form');
    if (chatForm) {
        chatForm.addEventListener('submit', handleFormSubmit);
    }
}

function setupWebSocket() {
    const chatId = getCurrentChatId();
    if (!chatId) {
        console.error('Cannot setup WebSocket without chat ID');
        return;
    }

    // Close existing connection if any
    if (chatWebSocket) {
        chatWebSocket.close();
    }

    // Create new WebSocket connection
    const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    const wsUrl = `${protocol}${window.location.host}/ws/chat/${chatId}`;

    chatWebSocket = new WebSocket(wsUrl);

    // WebSocket event handlers
    chatWebSocket.onopen = function() {
        console.log('WebSocket connection established');
        reconnectAttempts = 0; // Reset reconnect counter on successful connection
        clearTimeout(reconnectTimeout);
    };

    chatWebSocket.onmessage = function(event) {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'refresh_chat') {
                console.log('Received refresh notification');
                cargarChats(); // Refresh messages when notified
            }
        } catch (e) {
            console.error('Error processing WebSocket message:', e);
        }
    };

    chatWebSocket.onclose = function(event) {
        console.log('WebSocket connection closed:', event.code, event.reason);
        attemptReconnect();
    };

    chatWebSocket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
}

function attemptReconnect() {
    if (reconnectAttempts >= maxReconnectAttempts) {
        console.log('Max reconnection attempts reached');
        showError('Connection lost. Please refresh the page.');
        return;
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000); // Exponential backoff
    console.log(`Attempting to reconnect in ${delay}ms... (Attempt ${reconnectAttempts + 1}/${maxReconnectAttempts})`);

    reconnectTimeout = setTimeout(() => {
        reconnectAttempts++;
        setupWebSocket();
    }, delay);
}

async function handleFormSubmit(event) {
    event.preventDefault();

    try {
        const formData = new FormData(event.target);
        const mensaje = formData.get('mensaje');

        if (!mensaje || !mensaje.trim()) {
            console.warn('Message is empty');
            return;
        }

        const chatId = getCurrentChatId();
        if (!chatId) {
            throw new Error('Could not determine chat ID');
        }

        const response = await fetch(`/crud-chats/${chatId}/send`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to send message');
        }

        event.target.reset();
        await cargarChats();

    } catch (error) {
        console.error('Error sending message:', error);
        showError('Error sending message. Please try again.');
    }
}

async function cargarChats() {
    try {
        const chatId = getCurrentChatId();
        if (!chatId) {
            throw new Error('Chat ID not found');
        }

        const response = await fetch(`/mensajes?chatid=${chatId}`);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Error loading messages: ${response.statusText}`);
        }

        const mensajes = await response.json();
        const contenedor = document.getElementById('chat-messages');

        if (!contenedor) {
            throw new Error('Chat messages container not found');
        }

        // Clear container safely
        while (contenedor.firstChild) {
            contenedor.removeChild(contenedor.firstChild);
        }

        // Create document fragment for better performance
        const fragment = document.createDocumentFragment();

        mensajes.forEach(mensaje => {
            const messageDiv = document.createElement('div');
            messageDiv.className = `message-bubble ${mensaje.enviado ? 'user' : 'admin'}`;

            const messageP = document.createElement('p');
            messageP.textContent = mensaje.mensaje;

            messageDiv.appendChild(messageP);
            fragment.appendChild(messageDiv);
        });

        contenedor.appendChild(fragment);
        scrollToBottom();

    } catch (error) {
        console.error("Error loading messages:", error);
        showError(`Error loading messages: ${error.message}`);
    }
}

function scrollToBottom() {
    const chatContainer = document.querySelector('.chat-container') ||
        document.querySelector('.card-body');

    if (chatContainer) {
        chatContainer.scrollTo({
            top: chatContainer.scrollHeight,
            behavior: 'smooth'
        });
    }
}

function getCurrentChatId() {
    const pathParts = window.location.pathname.split('/');
    const chatId = pathParts[pathParts.length - 1];
    return /^\d+$/.test(chatId) ? chatId : null;
}

function showError(message) {
    // Remove existing error if any
    const existingError = document.querySelector('.chat-error-message');
    if (existingError) {
        existingError.remove();
    }

    const errorElement = document.createElement('div');
    errorElement.className = 'chat-error-message alert alert-danger';
    errorElement.textContent = message;
    errorElement.style.position = 'fixed';
    errorElement.style.top = '20px';
    errorElement.style.right = '20px';
    errorElement.style.zIndex = '1000';

    document.body.appendChild(errorElement);

    setTimeout(() => {
        errorElement.remove();
    }, 5000);
}

// Clean up WebSocket when page unloads
window.addEventListener('beforeunload', function() {
    if (chatWebSocket) {
        chatWebSocket.close();
    }
    clearTimeout(reconnectTimeout);
});