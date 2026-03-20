package com.hyhorde.arenapve.horde;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;

public final class HordeBossDamageScalingSystem extends EntityEventSystem<EntityStore, Damage> {
    private final HordeService hordeService;

    public HordeBossDamageScalingSystem(HordeService hordeService) {
        super(Damage.class);
        this.hordeService = hordeService;
    }

    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, Damage damage) {
        if (damage == null || damage.isCancelled()) {
            return;
        }
        EntityStore entityStore = (EntityStore)store.getExternalData();
        if (entityStore == null || !this.hordeService.isTrackingWorld(entityStore.getWorld())) {
            return;
        }
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource)) {
            return;
        }
        Ref<EntityStore> sourceRef = ((Damage.EntitySource)source).getRef();
        if (sourceRef == null) {
            return;
        }
        float damageMultiplier = this.hordeService.getTrackedBossDamageMultiplier(sourceRef);
        if (!Float.isFinite(damageMultiplier) || Math.abs(damageMultiplier - 1.0f) < 0.0001f) {
            return;
        }
        float baseAmount = Math.max(0.0f, damage.getAmount());
        float scaledAmount = Math.max(0.0f, baseAmount * damageMultiplier);
        damage.setAmount(scaledAmount);
    }

    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class));
    }
}
