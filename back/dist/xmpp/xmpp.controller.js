"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.XmppController = void 0;
const common_1 = require("@nestjs/common");
const xmpp_service_1 = require("./xmpp.service");
let XmppController = class XmppController {
    constructor(xmppService) {
        this.xmppService = xmppService;
    }
    async login(username, password) {
        return this.xmppService.connect(username, password);
    }
    async register(username, password) {
        return this.xmppService.registerUser(username, password);
    }
    async sendMessage(username, to, message) {
        return this.xmppService.sendMessage(username, to, message);
    }
    async sendFile(username, to, filePath) {
        return this.xmppService.sendFile(username, to, filePath);
    }
};
exports.XmppController = XmppController;
__decorate([
    (0, common_1.Post)('login'),
    __param(0, (0, common_1.Body)('username')),
    __param(1, (0, common_1.Body)('password')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String]),
    __metadata("design:returntype", Promise)
], XmppController.prototype, "login", null);
__decorate([
    (0, common_1.Post)('register'),
    __param(0, (0, common_1.Body)('username')),
    __param(1, (0, common_1.Body)('password')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String]),
    __metadata("design:returntype", Promise)
], XmppController.prototype, "register", null);
__decorate([
    (0, common_1.Post)('send-message'),
    __param(0, (0, common_1.Body)('username')),
    __param(1, (0, common_1.Body)('to')),
    __param(2, (0, common_1.Body)('message')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String, String]),
    __metadata("design:returntype", Promise)
], XmppController.prototype, "sendMessage", null);
__decorate([
    (0, common_1.Post)('send-file'),
    __param(0, (0, common_1.Body)('username')),
    __param(1, (0, common_1.Body)('to')),
    __param(2, (0, common_1.Body)('filePath')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [String, String, String]),
    __metadata("design:returntype", Promise)
], XmppController.prototype, "sendFile", null);
exports.XmppController = XmppController = __decorate([
    (0, common_1.Controller)('xmpp'),
    __metadata("design:paramtypes", [xmpp_service_1.XmppService])
], XmppController);
//# sourceMappingURL=xmpp.controller.js.map