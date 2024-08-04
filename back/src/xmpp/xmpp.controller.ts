import { Controller, Post, Body } from '@nestjs/common'
import { XmppService } from './xmpp.service'

@Controller('xmpp')
export class XmppController {
    constructor(private readonly xmppService: XmppService) {}

    @Post('login')
    async login(@Body('username') username: string, @Body('password') password: string) {
    return this.xmppService.connect(username, password);
    }

    @Post('register')
    async register(@Body('username') username: string, @Body('password') password: string) {
    return this.xmppService.registerUser(username, password);
    }

    @Post('send-message')
    async sendMessage(
    @Body('username') username: string,
    @Body('to') to: string,
    @Body('message') message: string,
    ) {
    return this.xmppService.sendMessage(username, to, message);
    }

    @Post('send-file')
    async sendFile(
    @Body('username') username: string,
    @Body('to') to: string,
    @Body('filePath') filePath: string,
    ) {
    return this.xmppService.sendFile(username, to, filePath);
    }
}
