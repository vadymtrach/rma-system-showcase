import { afterEach, describe, expect, it, vi } from "vitest";
import { connectWebSocket, disconnectWebSocket } from "./ws";

interface CapturedConfig {
  beforeConnect: () => Promise<void>;
  onConnect: () => void;
}

const captured: { config?: CapturedConfig } = {};

// A plain class rather than vi.fn(), so restoreAllMocks() between tests doesn't reset it.
vi.mock("@stomp/stompjs", () => ({
  Client: class {
    constructor(config: CapturedConfig) {
      captured.config = config;
    }
    activate() {}
    deactivate() {}
    subscribe() {}
  },
}));
vi.mock("sockjs-client", () => ({ default: vi.fn() }));

describe("connectWebSocket", () => {
  afterEach(() => disconnectWebSocket());

  it("checks the session before reconnecting, but not before the first connect", async () => {
    const beforeReconnect = vi.fn().mockResolvedValue(undefined);
    connectWebSocket(vi.fn(), beforeReconnect);
    const config = captured.config!;

    await config.beforeConnect();
    expect(beforeReconnect).not.toHaveBeenCalled();

    config.onConnect();
    await config.beforeConnect();
    expect(beforeReconnect).toHaveBeenCalledOnce();
  });

  it("refetches after a reconnect to catch up on missed updates", () => {
    const onRefresh = vi.fn();
    connectWebSocket(onRefresh, vi.fn().mockResolvedValue(undefined));
    const config = captured.config!;

    config.onConnect();
    expect(onRefresh).not.toHaveBeenCalled();

    config.onConnect();
    expect(onRefresh).toHaveBeenCalledOnce();
  });
});
