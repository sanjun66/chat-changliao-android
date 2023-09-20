package com.legend.baseui.ui.util.rom;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2018/07/03
 *    desc   :
 * </pre>
 */
public interface IChecker {

    ROM getRom();

    boolean checkManufacturer(String manufacturer);

    boolean checkRom();

    //boolean checkApplication(Context context);

    //boolean checkApplication(Set<String> installedPackages);

    ROMInfo checkBuildProp(RomProperties properties) throws Exception;
}
