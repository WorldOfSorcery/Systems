package me.hektortm.woSSystems;

import com.google.inject.AbstractModule;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.fishing.FishingManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;

public class WoSSystemsModule extends AbstractModule {
    @Override
    protected void configure() {
        // Bind each class to its implementation
        bind(WoSCore.class).toInstance(WoSCore.getPlugin(WoSCore.class)); // Singleton
        bind(LangManager.class).toProvider(() -> new LangManager(WoSCore.getPlugin(WoSCore.class)));
        bind(StatsManager.class).asEagerSingleton();
        bind(EcoManager.class).toInstance(new EcoManager(WoSSystems.getPlugin(WoSSystems.class)));
        bind(UnlockableManager.class).asEagerSingleton();
        bind(FishingManager.class).toProvider(() -> new FishingManager(WoSSystems.getPlugin(WoSSystems.class).fishingItemsFolder));
        bind(CitemManager.class).asEagerSingleton();
        bind(PlaceholderResolver.class).toProvider(() -> new PlaceholderResolver(new StatsManager(), new CitemManager()));
        bind(ConditionHandler.class).toProvider(() -> new ConditionHandler(
                new UnlockableManager(), new StatsManager(), new EcoManager(WoSSystems.getPlugin(WoSSystems.class)), new CitemManager()
        ));
        bind(InteractionManager.class).asEagerSingleton();
        bind(CRecipeManager.class).toProvider(() -> new CRecipeManager(new InteractionManager()));
        bind(LogManager.class).toProvider(() -> new LogManager(new LangManager(null), WoSCore.getPlugin(WoSCore.class)));
    }
}

