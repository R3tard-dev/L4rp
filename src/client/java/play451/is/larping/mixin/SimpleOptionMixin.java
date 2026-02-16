package play451.is.larping.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import play451.is.larping.ducks.ISimpleOption;

 
@Mixin(SimpleOption.class)
public class SimpleOptionMixin implements ISimpleOption {
    
    @Shadow
    @Final
    private java.util.function.Consumer<Object> changeCallback;
    
    @Shadow
    Object value;
    
    @Override
    public void larping$setValue(Object value) {
         
        this.value = value;
         
        this.changeCallback.accept(value);
    }
}