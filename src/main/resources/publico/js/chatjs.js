// js/chatjs.js

const chatBtn = document.getElementById('chat-btn');
const chatBox = document.getElementById('chat-box');
const closeChat = document.getElementById('close-chat');

// Manejo de la interfaz del chat
chatBtn.addEventListener('click', () => {
    chatBox.classList.toggle('d-none');
    chatBtn.style.display = 'none';
});

closeChat.addEventListener('click', () => {
    chatBox.classList.add('d-none');
    chatBtn.style.display = 'block';
});

// Función principal al cargar el DOM
document.addEventListener('DOMContentLoaded', function() {
    const chatMessages = document.getElementById('chat-messages');
    if (!chatMessages) return;

    const chatId = chatMessages.getAttribute('data-chatid');
    if (!chatId) {
        console.error('No se encontró el ID del chat');
        return;
    }

    // Cargar mensajes iniciales
    cargarChats(chatId);

    // Configurar WebSocket
    conectarWebSocketChat(chatId);

    // Manejar envío de mensajes
    document.getElementById('chatmensaje')?.addEventListener('submit', function(event) {
        event.preventDefault();

        const formData = new FormData(this);
        const currentChatId = formData.get('chat');
        const mensaje = formData.get('mensaje');

        if (!mensaje.trim()) return; // No enviar mensajes vacíos

        fetch('/send', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en el servidor');
                }
                return response.text();
            })
            .then(() => {
                cargarChats(currentChatId);
                document.querySelector('input[name="mensaje"]').value = '';
            })
            .catch(error => {
                console.error('Error al enviar mensaje:', error);
                alert('Error al enviar el mensaje. Inténtalo de nuevo.');
            });
    });

});

// Función mejorada para cargar chats
async function cargarChats(chatId) {
    try {
        if (!chatId) {
            const chatMessages = document.getElementById('chat-messages');
            chatId = chatMessages?.getAttribute('data-chatid');
            if (!chatId) throw new Error('No se pudo obtener el ID del chat');
        }

        const response = await fetch(`/mensajes?chatid=${chatId}`);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Error al cargar mensajes');
        }

        const mensajes = await response.json();
        const contenedor = document.getElementById('chat-messages');

        if (!contenedor) {
            throw new Error('No se encontró el contenedor de mensajes');
        }

        contenedor.innerHTML = ''; // Limpiar el contenedor

        mensajes.forEach(mensaje => {
            const messageDiv = document.createElement('div');
            messageDiv.className = `message-bubble ${mensaje.enviado ? 'admin' : 'user'}`;

            const messageP = document.createElement('p');
            messageP.textContent = mensaje.mensaje;

            messageDiv.appendChild(messageP);
            contenedor.appendChild(messageDiv);
        });

        scrollToBottom();

    } catch (error) {
        console.error("Error en cargarMensajes:", error);
        const errorElement = document.createElement('p');
        errorElement.className = 'text-danger';
        errorElement.textContent = `Error al cargar los Mensajes: ${error.message}`;

        const contenedor = document.getElementById('chat-messages');
        if (contenedor) {
            contenedor.innerHTML = '';
            contenedor.appendChild(errorElement);
        }
    }
}

// Función para hacer scroll al final del chat
function scrollToBottom() {
    const chatContainer = document.querySelector('.card-body');
    if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }
}

// WebSocket mejorado
let chatWebSocket = null;
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

function conectarWebSocketChat(chatId) {
    if (chatWebSocket) {
        chatWebSocket.close();
    }

    chatWebSocket = new WebSocket(`ws://${window.location.host}/ws/chat/${chatId}`);

    chatWebSocket.onopen = () => {
        console.log('Conexión WebSocket establecida');
        reconnectAttempts = 0; // Resetear intentos de reconexión
    };

    chatWebSocket.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'refresh_chat') {
                cargarChats(chatId); // Asegúrate de pasar el chatId
            }
        } catch (e) {
            console.error('Error procesando mensaje WebSocket:', e);
        }
    };

    chatWebSocket.onclose = (event) => {
        console.log('Conexión WebSocket cerrada:', event.code, event.reason);

        // Reconexión con retroceso exponencial
        if (reconnectAttempts < maxReconnectAttempts) {
            const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
            console.log(`Intentando reconectar en ${delay}ms...`);

            setTimeout(() => {
                reconnectAttempts++;
                conectarWebSocketChat(chatId);
            }, delay);
        } else {
            console.log('Máximo de intentos de reconexión alcanzado');
        }
    };

    chatWebSocket.onerror = (error) => {
        console.error('Error en WebSocket:', error);
    };
}