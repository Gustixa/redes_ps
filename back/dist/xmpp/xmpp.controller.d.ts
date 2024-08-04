import { XmppService } from './xmpp.service';
export declare class XmppController {
    private readonly xmppService;
    constructor(xmppService: XmppService);
    login(username: string, password: string): Promise<any>;
    register(username: string, password: string): Promise<void>;
    sendMessage(username: string, to: string, message: string): Promise<void>;
    sendFile(username: string, to: string, filePath: string): Promise<void>;
}
