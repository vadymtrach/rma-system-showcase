import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      "/api": "http://localhost:8080",
      "/ws": {
        target: "http://localhost:8080",
        ws: true,
      },
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    // A UTC+ zone, so date handling bugs that only show up around local midnight are caught.
    env: { TZ: "Europe/Warsaw" },
  },
});
