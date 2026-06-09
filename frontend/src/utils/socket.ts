import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const API_BASE_URL = (import.meta.env.VITE_API_URL as string) || 'http://localhost:5000/api';
const WEBSOCKET_URL = API_BASE_URL.replace(/\/api\/?$/, '') + '/ws';

class StompSocketWrapper {
  private listeners: Map<string, Array<(...args: any[]) => void>> = new Map();

  on(event: string, callback: (...args: any[]) => void) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)?.push(callback);
  }

  off(event: string, callback: (...args: any[]) => void) {
    const list = this.listeners.get(event);
    if (list) {
      this.listeners.set(event, list.filter((cb) => cb !== callback));
    }
  }

  emit(event: string, data: any) {
    console.log(`[STOMP Wrapper Emit] Event: ${event}`, data);
    
    if (event === 'typing_status' && stompClient && stompClient.connected) {
      // Expose typing mapping to destination
      stompClient.publish({
        destination: '/app/chat/typing',
        body: JSON.stringify(data),
      });
    }
    // 'join_chat' is a no-op as subscriptions handle queue routing natively in STOMP
  }

  trigger(event: string, ...args: any[]) {
    this.listeners.get(event)?.forEach((cb) => cb(...args));
  }
}

export let socket: any = new StompSocketWrapper();
export let stompClient: Client | null = null;

export const initSocket = (
  token: string,
  onNotification: (notif: any) => void,
  onMessage: (msg: any) => void
) => {
  if (stompClient) {
    stompClient.deactivate();
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS(`${WEBSOCKET_URL}?token=${token}`),
    debug: (str) => {
      console.log('[STOMP Debug]', str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = (frame) => {
    console.log('[STOMP] Connected successfully to Spring Boot WebSocket:', frame);

    // 1. Subscribe to User Notifications Queue
    stompClient?.subscribe('/user/queue/notifications', (message) => {
      try {
        const notif = JSON.parse(message.body);
        console.log('[STOMP] Received Notification:', notif);
        onNotification(notif);
        socket.trigger('new_notification', notif);
      } catch (err) {
        console.error('[STOMP Notification Parse Error]', err);
      }
    });

    // 2. Subscribe to User Chat Messages Queue
    stompClient?.subscribe('/user/queue/messages', (message) => {
      try {
        const msg = JSON.parse(message.body);
        console.log('[STOMP] Received Message:', msg);
        onMessage(msg);
        socket.trigger('new_message', msg);
      } catch (err) {
        console.error('[STOMP Message Parse Error]', err);
      }
    });

    // 3. Subscribe to Chat Typing Status Channel
    stompClient?.subscribe('/user/queue/typing', (message) => {
      try {
        const typingData = JSON.parse(message.body);
        console.log('[STOMP] Received Typing Status:', typingData);
        socket.trigger('typing_status', typingData);
      } catch (err) {
        console.error('[STOMP Typing Parse Error]', err);
      }
    });
  };

  stompClient.onStompError = (frame) => {
    console.error('[STOMP Error]', frame.headers['message']);
    console.error('[STOMP Details]', frame.body);
  };

  stompClient.onDisconnect = () => {
    console.log('[STOMP] Disconnected from server');
  };

  stompClient.activate();
  return socket;
};

export const disconnectSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
};
