import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let client: Client | null = null;

export function connectWebSocket(onRefresh: () => void): void {
  if (client) return;

  client = new Client({
    webSocketFactory: () => new SockJS("/ws"),
    reconnectDelay: 5000,
    onConnect: () => {
      client?.subscribe("/topic/rma-updates", (_message: IMessage) => {
        onRefresh();
      });
    },
  });

  client.activate();
}

export function disconnectWebSocket(): void {
  client?.deactivate();
  client = null;
}
