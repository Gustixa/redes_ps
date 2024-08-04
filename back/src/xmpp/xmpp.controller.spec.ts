import { Test, TestingModule } from '@nestjs/testing';
import { XmppController } from './xmpp.controller';

describe('XmppController', () => {
  let controller: XmppController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [XmppController],
    }).compile();

    controller = module.get<XmppController>(XmppController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
