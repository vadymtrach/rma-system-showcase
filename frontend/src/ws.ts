import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let client: Client | null = null;

/**
 * @param onRefresh called on every server update, and after a reconnect to catch up on
 *   updates missed while disconnected.
 * @param beforeReconnect called before each reconnect attempt; use it to check the session is
 *   still valid and call disconnectWebSocket() if not, which stops further attempts.
 */
export function connectWebSocket(onRefresh: () => void, beforeReconnect: () => Promise<void>): void {
  if (client) return;

  let connectedBefore = false;
  client = new Client({
    webSocketFactory: () => new SockJS("/ws"),
    reconnectDelay: 5000,
    beforeConnect: async () => {
      if (connectedBefore) await beforeReconnect();
    },
    onConnect: () => {
      client?.subscribe("/topic/rma-updates", (_message: IMessage) => {
        onRefresh();
      });
      if (connectedBefore) onRefresh();
      connectedBefore = true;
    },
  });

  client.activate();
}

export function disconnectWebSocket(): void {
  client?.deactivate();
  client = null;
}
