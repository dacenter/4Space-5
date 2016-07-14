package com.mattparks.space.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mattparks.space.core.builder.ICoreModule;
import com.mattparks.space.core.builder.celestials.ICorePlanet;
import com.mattparks.space.core.proxy.CommonProxy;
import com.mattparks.space.core.utils.SpaceCreativeTab;
import com.mattparks.space.core.utils.SpaceLog;
import com.mattparks.space.core.utils.SpaceVersionCheck;
import com.mattparks.space.venus.VenusCore;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

/**
 * The 4Space mods main entry class.
 */
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, dependencies = "required-after:GalacticraftCore;")
public class SpaceCore {
	@SidedProxy(clientSide = "com.mattparks.space.core.proxy.ClientProxy", serverSide = "com.mattparks.space.proxy.CommonProxy")
	public static CommonProxy proxy;

	@Instance(Constants.MOD_ID)
	public static SpaceCore instance;

	public static CreativeTabs spaceBlocksTab;
	public static CreativeTabs spaceItemsTab;
	
	public static List<ICoreModule> modulesList = new ArrayList<ICoreModule>();
	
	private static List<Block> blocksForCreativeTab = new ArrayList<Block>();
	private static List<Item> itemsForCreativeTab = new ArrayList<Item>();

	/**
	 * Registers a block with the game registry.
	 * 
	 * @param block The block to register.
	 * @param itemBlockClass The blocks item class.
	 * @param addToTabPool If true the block could be used as an create tab icon.
	 */
	public static void registerBlock(Block block, Class<? extends ItemBlock> itemBlockClass, boolean addToTabPool) {
		GameRegistry.registerBlock(block, itemBlockClass, block.getUnlocalizedName().replace("tile.", ""));
		
		if (addToTabPool) {
			blocksForCreativeTab.add(block);
		}
	}

	/**
	 * Registers a item with the game registry.
	 * 
	 * @param item The item to register.
	 * @param addToTabPool If true the item could be used as an create tab icon.
	 */
	public static void registerItem(Item item, boolean addToTabPool) {
		GameRegistry.registerItem(item, item.getUnlocalizedName().replace("item.", ""));

		if (addToTabPool) {
			itemsForCreativeTab.add(item);
		}
	}

	/**
	 * Registers hidden items with NEI.
	 */
	public static void registerHideNEI() {
		SpaceLog.severe("Hidding NEI Items/Blocks");
		
		for(ICoreModule module : modulesList) {
			module.hideNEI();
		}
	}
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		SpaceLog.severe("Pre-Init");
		
    	modulesList.add(new VenusCore());
    	
		proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
		SpaceLog.severe("Init");
		
		// Registers each module.
		for (ICoreModule module : modulesList) {
			module.loadBlocks();
			module.loadItems();
			
			module.addShapelessRecipes();
			module.registerTileEntities();
			module.registerCreatures();
			module.registerOtherEntities();
		}

		// Registers module planets.
		for (ICoreModule module : modulesList) {
			if (module instanceof ICorePlanet) {
				((ICorePlanet) module).registerPlanet();
			}
		}

		// Registers module moons.
	//	for (ICoreModule module : planetsList) {
	//		if (module instanceof ICoreMoon) {
	//			((ICoreMoon) module).registerMoon();
	//		}
	//	}
		
		proxy.init(event);
    }

	@EventHandler
    public void postInit(FMLPostInitializationEvent event) {
		SpaceLog.severe("Post-Init");
		
		createCreativeTabs();
		
		for (ICoreModule planet : modulesList) {
			planet.loadRecipes();
		}
		
		proxy.postInit(event);
	}
	
	private void createCreativeTabs() {
		Random random = new Random(1447);
		
		Block blockSelected = Blocks.dirt;
		Item itemSelected = Items.chest_minecart;
		
		if (!blocksForCreativeTab.isEmpty()) {
			blockSelected = blocksForCreativeTab.get(random.nextInt(blocksForCreativeTab.size()));
			blocksForCreativeTab.clear(); // TODO: Select meta data!
		}
		
		if (!itemsForCreativeTab.isEmpty()) {
			itemSelected = itemsForCreativeTab.get(random.nextInt(itemsForCreativeTab.size()));
			itemsForCreativeTab.clear(); // TODO: Select meta data?
		}
		
		spaceBlocksTab = new SpaceCreativeTab(CreativeTabs.getNextID(), "SpaceBlocks", Item.getItemFromBlock(blockSelected), 0);
		spaceItemsTab = new SpaceCreativeTab(CreativeTabs.getNextID(), "SpaceItems", itemSelected, 0);
	}

	@EventHandler
	public static void preLoad(FMLPreInitializationEvent preEvent) {
		proxy.registerRenderInfo();
	}
	
    @EventHandler
    public void serverInit(FMLServerStartedEvent event) {
		SpaceLog.severe("Server-Init");
		
    	if (Constants.CHECK_VERSIONS) {
    		SpaceVersionCheck.startCheck();
    	}
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
		SpaceLog.severe("Server-Starting");
    }
    
    @EventHandler
    public void unregisterDims(FMLServerStoppedEvent event) {
		SpaceLog.severe("Unregister-Dims");
    }
}
