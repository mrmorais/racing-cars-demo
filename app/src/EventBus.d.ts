declare interface Options {
  vertxbus_ping_interval?: number;
  vertxbus_reconnect_attempts_max?: number;
  vertxbus_reconnect_delay_min?: number;
  vertxbus_reconnect_delay_max?: number;
  vertxbus_reconnect_exponent?: number;
  vertxbus_randomization_factor?: number;
}

declare interface Message {
  address: string;
  body: any;
  type: string;
}

declare class EventBus {
  pingInterval: number;
  pingTimerID: any;
  reconnectEnabled: boolean;
  reconnectAttempts: number;
  reconnectTimerID: any;
  onopen: () => any;

  constructor(url: string, options?: Options);

  send(address: string, message: any, headers?: any, callback?: any);
  registerHandler(address: string, headers?: any, callback?: (_: any, message: Message) => any);
}
