import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { XmppController } from './xmpp/xmpp.controller';
import { XmppService } from './xmpp/xmpp.service';
import { XmppModule } from './xmpp/xmpp.module';

@Module({
  imports: [XmppModule],
  controllers: [AppController, XmppController],
  providers: [AppService, XmppService],
})
export class AppModule {}
