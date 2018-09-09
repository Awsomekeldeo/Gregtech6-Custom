package gregapi.tileentity.base;

import static gregapi.data.CS.*;

import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.block.multitileentity.IMultiTileEntity.*;
import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.code.ArrayListNoNulls;
import gregapi.network.IPacket;
import gregapi.network.packets.data.PacketSyncDataByte;
import gregapi.network.packets.data.PacketSyncDataByteArray;
import gregapi.network.packets.data.PacketSyncDataInteger;
import gregapi.network.packets.data.PacketSyncDataLong;
import gregapi.network.packets.data.PacketSyncDataName;
import gregapi.network.packets.data.PacketSyncDataShort;
import gregapi.network.packets.ids.PacketSyncDataByteAndIDs;
import gregapi.network.packets.ids.PacketSyncDataByteArrayAndIDs;
import gregapi.network.packets.ids.PacketSyncDataIDs;
import gregapi.network.packets.ids.PacketSyncDataIntegerAndIDs;
import gregapi.network.packets.ids.PacketSyncDataLongAndIDs;
import gregapi.network.packets.ids.PacketSyncDataShortAndIDs;
import gregapi.render.IRenderedBlockObject;
import gregapi.render.IRenderedBlockObjectSideCheck;
import gregapi.util.ST;
import gregapi.util.UT;
import gregapi.util.WD;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

/**
 * @author Gregorius Techneticies
 */
public abstract class TileEntityBase04MultiTileEntities extends TileEntityBase03TicksAndSync implements IRenderedBlockObjectSideCheck, IRenderedBlockObject, IMTE_OnPainting, IMTE_OnNeighborBlockChange, IMTE_GetPickBlock, IMTE_OnRegistrationFirst, IMTE_RecolourBlock, IMTE_GetDrops, IMTE_OnBlockActivated, IMTE_ShouldSideBeRendered, IMTE_GetFlammability, IMTE_GetFireSpreadSpeed {
	private short mMTEID = W, mMTERegistry = W;
	private String mCustomName = null;
	
	/** return the internal Name of this TileEntity to be registered. DO NOT START YOUR NAME WITH "gt."!!! */
	public abstract String getTileEntityName();
	
	@Override
	public void onRegistrationFirst(MultiTileEntityRegistry aRegistry, short aID) {
		GameRegistry.registerTileEntity(getClass(), getTileEntityName());
	}
	
	@Override
	public IPacket getClientDataPacket(boolean aSendAll)								{return aSendAll ? new PacketSyncDataIDs				(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID()				) : null;}
	public IPacket getClientDataPacketByte(boolean aSendAll, byte aByte)				{return aSendAll ? new PacketSyncDataByteAndIDs			(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID(), aByte			) : new PacketSyncDataByte		(getCoords(), aByte			);}
	public IPacket getClientDataPacketShort(boolean aSendAll, short aShort)				{return aSendAll ? new PacketSyncDataShortAndIDs		(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID(), aShort		) : new PacketSyncDataShort		(getCoords(), aShort		);}
	public IPacket getClientDataPacketInteger(boolean aSendAll, int aInteger)			{return aSendAll ? new PacketSyncDataIntegerAndIDs		(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID(), aInteger		) : new PacketSyncDataInteger	(getCoords(), aInteger		);}
	public IPacket getClientDataPacketLong(boolean aSendAll, long aLong)				{return aSendAll ? new PacketSyncDataLongAndIDs			(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID(), aLong			) : new PacketSyncDataLong		(getCoords(), aLong			);}
	public IPacket getClientDataPacketByteArray(boolean aSendAll, byte... aByteArray)	{return aSendAll ? new PacketSyncDataByteArrayAndIDs	(getCoords(), getMultiTileEntityRegistryID(), getMultiTileEntityID(), aByteArray	) : new PacketSyncDataByteArray	(getCoords(), aByteArray	);}
	
	@Override
	public final void initFromNBT(NBTTagCompound aNBT, short aMTEID, short aMTERegistry) {
		// Set ID and Registry ID.
		mMTEID = aMTEID;
		mMTERegistry = aMTERegistry;
		// Read the Default Parameters from NBT.
		if (aNBT != null) readFromNBT(aNBT);
	}
	
	@Override
	public final void readFromNBT(NBTTagCompound aNBT) {
		// Check if this is a World/Chunk Loading Process calling readFromNBT.
		if (mMTEID == W || mMTERegistry == W) {
			// Yes it is, so read the ID Tags first.
			mMTEID = aNBT.getShort(NBT_MTE_ID);
			mMTERegistry = aNBT.getShort(NBT_MTE_REG);
			// And add additional Default Parameters, in case the Mod updated with new ones.
			MultiTileEntityRegistry tRegistry = MultiTileEntityRegistry.getRegistry(mMTERegistry);
			if (tRegistry != null) {
				MultiTileEntityClassContainer tClass = tRegistry.getClassContainer(mMTEID);
				if (tClass != null) {
					// Add the Default Parameters.
					aNBT = UT.NBT.fuse(aNBT, tClass.mParameters);
				}
			}
		}
		// read the Coords if it has them.
		if (aNBT.hasKey("x")) xCoord = aNBT.getInteger("x");
		if (aNBT.hasKey("y")) yCoord = aNBT.getInteger("y");
		if (aNBT.hasKey("z")) zCoord = aNBT.getInteger("z");
		// read the custom Name.
		if (aNBT.hasKey("display")) mCustomName = aNBT.getCompoundTag("display").getString("Name");
		// And now your custom readFromNBT.
		try {readFromNBT2(aNBT);} catch(Throwable e) {e.printStackTrace(ERR);}
	}
	
	public void readFromNBT2(NBTTagCompound aNBT) {/**/}
	
	@Override
	public final void writeToNBT(NBTTagCompound aNBT) {
		super.writeToNBT(aNBT);
		// write the IDs
		aNBT.setShort(NBT_MTE_ID, mMTEID);
		aNBT.setShort(NBT_MTE_REG, mMTERegistry);
		// write the Custom Name
		if (UT.Code.stringValid(mCustomName)) aNBT.setTag("display", UT.NBT.makeString(aNBT.getCompoundTag("display"), "Name", mCustomName));
		if (isPainted()) {aNBT.setInteger(NBT_COLOR, getPaint()); aNBT.setBoolean(NBT_PAINTED, T);}
		// write the rest
		try {writeToNBT2(aNBT);} catch(Throwable e) {e.printStackTrace(ERR);}
	}
	
	public void writeToNBT2(NBTTagCompound aNBT) {/**/}
	
	@Override
	public NBTTagCompound writeItemNBT(NBTTagCompound aNBT) {
		if (UT.Code.stringValid(mCustomName)) aNBT.setTag("display", UT.NBT.makeString(aNBT.getCompoundTag("display"), "Name", mCustomName));
		if (UT.Code.stringValid(ERROR_MESSAGE) && isClientSide()) aNBT.setTag("display", UT.NBT.makeString(aNBT.getCompoundTag("display"), "Name", ERROR_MESSAGE));
		if (isPainted()) {aNBT.setInteger(NBT_COLOR, getPaint()); aNBT.setBoolean(NBT_PAINTED, T);}
		return aNBT;
	}
	
	@Override
	public void sendClientData(boolean aSendAll, EntityPlayerMP aPlayer) {
		super.sendClientData(aSendAll, aPlayer);
		if (aSendAll && UT.Code.stringValid(mCustomName)) {
			if (aPlayer == null) {
				getNetworkHandler().sendToAllPlayersInRange(new PacketSyncDataName(getCoords(), mCustomName), worldObj, getCoords());
			} else {
				getNetworkHandler().sendToPlayer(new PacketSyncDataName(getCoords(), mCustomName), aPlayer);
			}
		}
	}
	
	@Override
	public final boolean onBlockActivated(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
		try {
			return allowRightclick(aPlayer) && (checkObstruction(aPlayer, aSide, aHitX, aHitY, aHitZ) || onBlockActivated2(aPlayer, aSide, aHitX, aHitY, aHitZ));
		} catch(Throwable e) {
			e.printStackTrace(ERR);
			return T;
		}
	}
	
	public boolean onBlockActivated2(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
		return F;
	}
	
	public boolean checkObstruction(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
		return !(aPlayer == null || aPlayer instanceof FakePlayer || SIDES_INVALID[aSide] || !WD.obstructed(worldObj, xCoord, yCoord, zCoord, aSide));
	}
	
	@Override
	public ArrayListNoNulls<ItemStack> getDrops(int aFortune, boolean aSilkTouch) {
		ArrayListNoNulls<ItemStack> rList = new ArrayListNoNulls();
		MultiTileEntityRegistry tRegistry = MultiTileEntityRegistry.getRegistry(mMTERegistry);
		if (tRegistry != null) rList.add(tRegistry.getItem(mMTEID, writeItemNBT(UT.NBT.make())));
		return rList;
	}
	public void popOff() {
		for (ItemStack tStack : getDrops(0, F)) ST.drop(worldObj, xCoord, yCoord, zCoord, tStack);
		setToAir();
	}
	public void popOff(Entity aEntity) {
		for (ItemStack tStack : getDrops(0, F)) ST.drop(aEntity.worldObj, aEntity.posX, aEntity.posY, aEntity.posZ, tStack);
		setToAir();
	}
	public void popOff(World aWorld, double aX, double aY, double aZ) {
		for (ItemStack tStack : getDrops(0, F)) ST.drop(aWorld, aX, aY, aZ, tStack);
		setToAir();
	}
	public void popOff(World aWorld, ChunkCoordinates aCoords) {
		for (ItemStack tStack : getDrops(0, F)) ST.drop(aWorld, aCoords, tStack);
		setToAir();
	}
	
	public ItemStack toStack() {
		MultiTileEntityRegistry tRegistry = MultiTileEntityRegistry.getRegistry(mMTERegistry);
		if (tRegistry != null) return tRegistry.getItem(mMTEID, writeItemNBT(UT.NBT.make()));
		return null;
	}
	
	@Override
	public void onNeighborBlockChange(World aWorld, Block aBlock) {
		mBlockUpdated = T;
	}
	
	@Override
	public boolean recolourBlock(byte aSide, byte aColor) {
		if (UT.Code.exists(aColor, DYES_INVERTED)) {
			int aRGB = (isPainted() ? UT.Code.mixRGBInt(DYES_INT_INVERTED[aColor], getPaint()) : DYES_INT_INVERTED[aColor]) & ALL_NON_ALPHA_COLOR;
			if (paint(aRGB)) {updateClientData(); causeBlockUpdate(); return T;}
			return F;
		}
		if (unpaint()) {updateClientData(); causeBlockUpdate(); return T;}
		return F;
	}
	
	@Override
	public boolean onPainting(byte aSide, int aRGB) {
		if (paint(aRGB)) {updateClientData(); causeBlockUpdate(); return T;}
		return F;
	}
	
	public boolean unpaint() {return F;}
	public boolean isPainted() {return F;}
	public boolean paint(int aRGB) {return F;}
	public int getPaint() {return UNCOLORED;}
	
	public boolean removePaint(byte aSide) {return unpaint();}
	
	@Override public String getCustomName() {return UT.Code.stringValid(mCustomName) ? mCustomName : null;}
	@Override public void setCustomName(String aName) {mCustomName = aName;}
	@Override public ItemStack getPickBlock(MovingObjectPosition aTarget) {MultiTileEntityRegistry tRegistry = MultiTileEntityRegistry.getRegistry(mMTERegistry); return tRegistry == null ? null : tRegistry.getItem(mMTEID, writeItemNBT(UT.NBT.make()));}
	@Override public short getMultiTileEntityID() {return mMTEID;}
	@Override public short getMultiTileEntityRegistryID() {return mMTERegistry;}
	@Override public void setShouldRefresh(boolean aShouldRefresh) {mShouldRefresh = aShouldRefresh;}
}