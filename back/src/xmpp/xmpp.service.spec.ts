import { Test, TestingModule } from '@nestjs/testing';
import { XmppService } from './xmpp.service';

describe('XmppService', () => {
  let service: XmppService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [XmppService],
    }).compile();

    service = module.get<XmppService>(XmppService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
