package com.artillexstudios.axvanish.api.group.packet;

import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketType;
import com.artillexstudios.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axapi.utils.position.BlockPosition;

public final class ClientboundBlockEventWrapper extends PacketWrapper {
    private BlockPosition position;
    private int actionId;
    private int actionData;
    private int blockType;

    public ClientboundBlockEventWrapper(BlockPosition position, int blockType, int actionData, int actionId) {
        this.position = position;
        this.blockType = blockType;
        this.actionData = actionData;
        this.actionId = actionId;
    }

    public ClientboundBlockEventWrapper(PacketEvent event) {
        super(event);
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public void setPosition(BlockPosition position) {
        this.position = position;
    }

    public int getActionId() {
        return this.actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getActionData() {
        return this.actionData;
    }

    public void setActionData(int actionData) {
        this.actionData = actionData;
    }

    public int getBlockType() {
        return this.blockType;
    }

    public void setBlockType(int blockType) {
        this.blockType = blockType;
    }

    @Override
    public void write(FriendlyByteBuf out) {
        out.writeBlockPos(this.position);
        out.writeByte(this.actionId);
        out.writeByte(this.actionData);
        out.writeVarInt(this.blockType);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.position = buf.readBlockPosition();
        this.actionId = buf.readUnsignedByte();
        this.actionData = buf.readUnsignedByte();
        this.blockType = buf.readVarInt();
    }

    @Override
    public PacketType packetType() {
        return ClientboundPacketTypes.BLOCK_EVENT;
    }
}
