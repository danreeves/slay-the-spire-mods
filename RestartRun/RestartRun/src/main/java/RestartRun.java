import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.helpers.CardHelper;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.screens.DungeonTransitionScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;

@SpireInitializer
public class RestartRun {

    public RestartRun() {
    }

    public static void initialize() {
    }

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CLASS)
    public static class RestartButton {
        public static ReturnToMenuButton restartButton = new ReturnToMenuButton();
        public static ReturnToMenuButton newGameButton = new ReturnToMenuButton();

        public static void restart(boolean newSeed) {
            // Code taken from erasels who did the research for this.
            // Modified to maintain the current seed
            // https://github.com/erasels/QuickRestart/blob/3b9650dccad19ef3cfa88dd60f843801c0b66e99/src/main/java/quickRestart/helper/RestartRunHelper.java#L22
            CardCrawlGame.music.fadeAll();
            if (Settings.AMBIANCE_ON) {
                CardCrawlGame.sound.stop("WIND");
            }
            AbstractDungeon.getCurrRoom().clearEvent();
            AbstractDungeon.closeCurrentScreen();
            CardCrawlGame.dungeonTransitionScreen = new DungeonTransitionScreen(Exordium.ID);

            AbstractDungeon.reset();
            Settings.hasEmeraldKey = false;
            Settings.hasRubyKey = false;
            Settings.hasSapphireKey = false;
            ShopScreen.resetPurgeCost();
            CardCrawlGame.tips.initialize();
            CardCrawlGame.metricData.clearData();
            CardHelper.clear();
            TipTracker.refresh();
            System.gc();

            if (CardCrawlGame.chosenCharacter == null) {
                CardCrawlGame.chosenCharacter = AbstractDungeon.player.chosenClass;
            }

            if (newSeed) {
                Long sTime = System.nanoTime();
                Random rng = new Random(sTime);
                Settings.seedSourceTimestamp = sTime;
                Settings.seed = SeedHelper.generateUnoffensiveSeed(rng);
                SeedHelper.cachedSeed = null;
            }

            AbstractDungeon.generateSeeds();

            CardCrawlGame.mode = CardCrawlGame.GameMode.CHAR_SELECT;

        }

    }

    @SpirePatch(clz = DeathScreen.class, method = "update")
    public static class UpdateRestartButton {
        public static void Postfix(DeathScreen __instance) {
            if (!RestartButton.restartButton.show) {
                RestartButton.restartButton.appear(Settings.WIDTH / 2.0F + 250.0F * Settings.scale,
                        Settings.HEIGHT * 0.15F, "Restart seed");
            }
            if (!RestartButton.newGameButton.show) {
                RestartButton.newGameButton.appear(Settings.WIDTH / 2.0F - 250.0F * Settings.scale,
                        Settings.HEIGHT * 0.15F, "New Game");
            }
            RestartButton.restartButton.update();
            RestartButton.newGameButton.update();

            if (RestartButton.restartButton.hb.clicked) {
                RestartButton.restartButton.hb.clicked = false;
                RestartButton.restartButton.hide();
                RestartButton.newGameButton.hide();
                RestartButton.restart(false);
            }
            if (RestartButton.newGameButton.hb.clicked) {
                RestartButton.newGameButton.hb.clicked = false;
                RestartButton.newGameButton.hide();
                RestartButton.restartButton.hide();
                RestartButton.restart(true);
            }
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "render")
    public static class RenderRestartButton {
        public static void Postfix(DeathScreen __instance, SpriteBatch sb) {
            RestartButton.restartButton.render(sb);
            RestartButton.newGameButton.render(sb);
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "reopen", paramtypez = { boolean.class })
    public static class ReopenRestartButton {
        public static void Postfix(DeathScreen __instance, boolean fromVictoryUnlock) {
            MoveRestartButton.statsTimer = 0.0F;
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "updateStatsScreen")
    public static class MoveRestartButton {
        public static float statsTimer = 0.5F;

        public static void Postfix(DeathScreen __instance, boolean ___showingStats) {
            if (___showingStats) {
                MoveRestartButton.statsTimer -= Gdx.graphics.getDeltaTime();
                if (MoveRestartButton.statsTimer < 0.0F) {
                    MoveRestartButton.statsTimer = 0.0F;
                }

                RestartButton.restartButton.y = Interpolation.pow3In.apply(Settings.HEIGHT * 0.1F,
                        Settings.HEIGHT * 0.15F, MoveRestartButton.statsTimer * 1.0F / 0.5F);
                RestartButton.newGameButton.y = Interpolation.pow3In.apply(Settings.HEIGHT * 0.1F,
                        Settings.HEIGHT * 0.15F, MoveRestartButton.statsTimer * 1.0F / 0.5F);
            }
        }
    }

}
