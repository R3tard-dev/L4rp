package play451.is.larping;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Larp implements ModInitializer {
	public static final String MOD_ID = "l4rp";

	 
	 
	 
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("L4rp initialized");
	}
}