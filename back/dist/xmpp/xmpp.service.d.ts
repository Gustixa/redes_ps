export declare class XmppService {
    private clients;
    connect(username: string, password: string): Promise<any>;
    sendMessage(username: string, to: string, message: string): Promise<void>;
    sendFile(username: string, to: string, filePath: string): Promise<void>;
    registerUser(username: string, password: string): Promise<void>;
}
