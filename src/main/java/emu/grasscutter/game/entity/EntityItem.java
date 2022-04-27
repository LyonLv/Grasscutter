package emu.grasscutter.game.entity;

import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.game.Player;
import emu.grasscutter.game.Scene;
import emu.grasscutter.game.World;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.net.proto.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import emu.grasscutter.net.proto.AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair;
import emu.grasscutter.net.proto.EntityAuthorityInfoOuterClass.EntityAuthorityInfo;
import emu.grasscutter.net.proto.EntityClientDataOuterClass.EntityClientData;
import emu.grasscutter.net.proto.EntityRendererChangedInfoOuterClass.EntityRendererChangedInfo;
import emu.grasscutter.net.proto.GadgetBornTypeOuterClass.GadgetBornType;
import emu.grasscutter.net.proto.MotionInfoOuterClass.MotionInfo;
import emu.grasscutter.net.proto.PropPairOuterClass.PropPair;
import emu.grasscutter.net.proto.ProtEntityTypeOuterClass.ProtEntityType;
import emu.grasscutter.net.proto.SceneEntityAiInfoOuterClass.SceneEntityAiInfo;
import emu.grasscutter.net.proto.SceneEntityInfoOuterClass.SceneEntityInfo;
import emu.grasscutter.net.proto.SceneGadgetInfoOuterClass.SceneGadgetInfo;
import emu.grasscutter.net.proto.VectorOuterClass.Vector;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.ProtoHelper;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

public class EntityItem extends EntityGadget {
	private final Position pos;
	private final Position rot;
	
	private final GameItem item;
	private final long guid;
	
	public EntityItem(Scene scene, Player player, ItemData itemData, Position pos, int count) {
		super(scene);
		this.id = getScene().getWorld().getNextEntityId(EntityIdType.GADGET);
		this.pos = new Position(pos);
		this.rot = new Position();
		this.guid = player.getNextGameGuid();
		this.item = new GameItem(itemData, count);
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	private GameItem getItem() {
		return this.item;
	}

	public ItemData getItemData() {
		return this.getItem().getItemData();
	}

	public long getGuid() {
		return guid;
	}

	public int getCount() {
		return this.getItem().getCount();
	}
	
	@Override
	public int getGadgetId() {
		return this.getItemData().getGadgetId();
	}

	@Override
	public Position getPosition() {
		return this.pos;
	}

	@Override
	public Position getRotation() {
		return this.rot;
	}
	
	@Override
	public Int2FloatOpenHashMap getFightProperties() {
		return null;
	}

	@Override
	public SceneEntityInfo toProto() {
		EntityAuthorityInfo authority = EntityAuthorityInfo.newBuilder()
				.setAbilityInfo(AbilitySyncStateInfo.newBuilder())
				.setRendererChangedInfo(EntityRendererChangedInfo.newBuilder())
				.setAiInfo(SceneEntityAiInfo.newBuilder().setIsAiOpen(true).setBornPos(Vector.newBuilder()))
				.setBornPos(Vector.newBuilder())
				.build();
		
		SceneEntityInfo.Builder entityInfo = SceneEntityInfo.newBuilder()
				.setEntityId(getId())
				.setEntityType(ProtEntityType.PROT_ENTITY_GADGET)
				.setMotionInfo(MotionInfo.newBuilder().setPos(getPosition().toProto()).setRot(getRotation().toProto()).setSpeed(Vector.newBuilder()))
				.addAnimatorParaList(AnimatorParameterValueInfoPair.newBuilder())
				.setEntityClientData(EntityClientData.newBuilder())
				.setEntityAuthorityInfo(authority)
				.setLifeState(1);
		
		PropPair pair = PropPair.newBuilder()
				.setType(PlayerProperty.PROP_LEVEL.getId())
				.setPropValue(ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, 1))
				.build();
		entityInfo.addPropList(pair);
		
		SceneGadgetInfo.Builder gadgetInfo = SceneGadgetInfo.newBuilder()
				.setGadgetId(this.getItemData().getGadgetId())
				.setTrifleItem(this.getItem().toProto())
				.setBornType(GadgetBornType.GADGET_BORN_IN_AIR)
				.setAuthorityPeerId(this.getWorld().getHostPeerId())
				.setIsEnableInteract(true);

		entityInfo.setGadget(gadgetInfo);
		
		return entityInfo.build();
	}
}
