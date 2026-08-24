package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.item.ModItems;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import com.nedraw.upgrading.item.ZSlotItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class ZSlotModelHandler {

    // Store baked models for each component at bake time
    private static final Map<String, BakedModel> FRAME_MODELS  = new HashMap<>();
    private static final Map<String, BakedModel> BOARD_MODELS  = new HashMap<>();
    private static final Map<String, BakedModel> CHIP_MODELS   = new HashMap<>();

    private static final String[] FRAMES = {
            "fabric", "wooden", "copper", "iron", "golden", "amethyst",
            "cactus", "glass", "rose_gold", "sponge", "mushroom", "void"
    };
    private static final String[] BOARDS = {
            "basic", "ender", "enchanted", "piston", "wool", "corrupted"
    };
    public static final String[] CHIPS = {
            "basic", "heart", "diamond", "spade", "club", "food", "portal", "dark"
    };

    @SubscribeEvent
    public static void onModelsBaked(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        // Cache all component models
        for (String frame : FRAMES) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "frame_" + frame);
            BakedModel model = models.get(new ModelResourceLocation(loc, "inventory"));
            if (model != null) FRAME_MODELS.put(frame, model);
        }
        for (String board : BOARDS) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "board_" + board);
            BakedModel model = models.get(new ModelResourceLocation(loc, "inventory"));
            if (model != null) BOARD_MODELS.put(board, model);
        }
        for (String chip : CHIPS) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "item/chip_" + chip + "_zslot");
            BakedModel model = models.get(new ModelResourceLocation(loc, "standalone"));
            if (model != null) CHIP_MODELS.put(chip, model);
        }

        // Replace Z-Slot model
        ResourceLocation zSlotLoc = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "z_slot");
        ModelResourceLocation zSlotModelLoc = new ModelResourceLocation(zSlotLoc, "inventory");
        BakedModel original = models.get(zSlotModelLoc);
        if (original != null) {
            models.put(zSlotModelLoc, new ZSlotBakedModel(original));
        }
    }

    // =====================
    // INNER BAKED MODEL
    // =====================
    public static class ZSlotBakedModel implements BakedModel {
        private final BakedModel base;

        public ZSlotBakedModel(BakedModel base) {
            this.base = base;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            return getQuads(state, side, rand, ModelData.EMPTY, null);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, net.minecraft.client.renderer.RenderType type) {
            return base.getQuads(state, side, rand, data, type);
        }

        @Override
        public boolean useAmbientOcclusion() { return false; }
        @Override
        public boolean isGui3d() { return false; }
        @Override
        public boolean usesBlockLight() { return false; }
        @Override
        public boolean isCustomRenderer() { return false; }
        @Override
        public TextureAtlasSprite getParticleIcon() { return base.getParticleIcon(); }
        @Override
        public ItemTransforms getTransforms() { return base.getTransforms(); }

        @Override
        public ItemOverrides getOverrides() {
            return new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel model, ItemStack stack,
                                          net.minecraft.client.multiplayer.ClientLevel level,
                                          net.minecraft.world.entity.LivingEntity entity, int seed) {
                    String frame = ZSlotItem.getFrame(stack);
                    String board = ZSlotItem.getBoard(stack);
                    String chip  = ZSlotItem.getChip(stack);

                    BakedModel frameModel = FRAME_MODELS.getOrDefault(frame, base);
                    BakedModel boardModel = BOARD_MODELS.getOrDefault(board, base);
                    BakedModel chipModel  = CHIP_MODELS.getOrDefault(chip, base);

                    return new ZSlotLayeredModel(boardModel, chipModel, frameModel, base);
                }
            };
        }
    }

    // =====================
    // LAYERED MODEL
    // =====================
    public static class ZSlotLayeredModel implements BakedModel {
        private final BakedModel board, chip, frame, base;

        public ZSlotLayeredModel(BakedModel board, BakedModel chip, BakedModel frame, BakedModel base) {
            this.board = board;
            this.chip  = chip;
            this.frame = frame;
            this.base  = base;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            return getQuads(state, side, rand, ModelData.EMPTY, null);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, net.minecraft.client.renderer.RenderType type) {
            List<BakedQuad> quads = new ArrayList<>();
            quads.addAll(board.getQuads(state, side, rand, data, type));
            quads.addAll(chip.getQuads(state, side, rand, data, type));
            quads.addAll(frame.getQuads(state, side, rand, data, type));
            return quads;
        }

        @Override
        public boolean useAmbientOcclusion() { return false; }
        @Override
        public boolean isGui3d() { return false; }
        @Override
        public boolean usesBlockLight() { return false; }
        @Override
        public boolean isCustomRenderer() { return false; }
        @Override
        public TextureAtlasSprite getParticleIcon() { return base.getParticleIcon(); }
        @Override
        public ItemTransforms getTransforms() { return base.getTransforms(); }
        @Override
        public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    }
}