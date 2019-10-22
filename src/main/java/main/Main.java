package main;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import profile.Profile;

import static net.sourceforge.argparse4j.impl.Arguments.store;
import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class Main {

    private static int filter = 1; // filter strength 1,2 or 3 where 1 is loose 2 is normal and 3 is strict

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        Namespace input = null;

        ArgumentParser parser = ArgumentParsers.newFor("NoPeak").build()
                .defaultHelp(true)
                .description("NoPeak: Motif discovery without peak calling.\n" +
                        "Known modes: PROFILE, LOGO\n" +
                        "Example: java -jar noPeak.jar PROFILE --reads reads.bed --genome hg19/\n" +
                        "Example: java -jar noPeak.jar LOGO --signal reads.csv --fraglen 100\n");

        Subparsers subparsers = parser.addSubparsers();

        Subparser parserProfile = subparsers.addParser("PROFILE").setDefault("mode", "PROFILE");

        parserProfile.addArgument("--reads").help("Path to reads").action(store()).required(true);
        parserProfile.addArgument("-g", "--genome").help("Path to folder with the chromosomes as fasta files.").action(store()).required(true);
        parserProfile.addArgument("-k", "--k-mer").dest("k").type(Integer.class).help("Size of K-mers to identify. Default: 8").setDefault(8).action(store());
        parserProfile.addArgument("-t", "--threads").type(Integer.class).help("Number of threads. Maximum is 24. Default: 2").setDefault(2).action(store());
        parserProfile.addArgument("-r", "--radius").type(Integer.class).help("Radius to scan around each read. Default: 500").setDefault(500).action(store());

        Subparser parserLogo = subparsers.addParser("LOGO").setDefault("mode", "LOGO");

        parserLogo.addArgument("-s", "--signal").help("Path to signal profiles generated by NoPeak PROFILE").action(store()).required(true);
        parserLogo.addArgument("-c", "--control").help("Path to control profiles generated by NoPeak PROFILE").action(store());
        parserLogo.addArgument("-f", "--fraglen").type(Integer.class).help("Estimated fragment length. You can use the estimateFraglen.jar tool").action(store()).required(true);
        parserLogo.addArgument("--gui").help("Show graphical interface to adjust profile filters").action(storeTrue());
        parserLogo.addArgument("--export-kmers").help("Exports the k-mer list to the given file").action(store());
        parserLogo.addArgument("--loose").help("Apply a loose filtering for profile shape.").action(storeTrue());
        parserLogo.addArgument("--strict").help("Apply a strict filtering for profile shape.").action(storeTrue());

        try {
            input = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }


        if (input.getBoolean("loose") && (input.getBoolean("strict"))) {
            System.err.println("Please only set the filter to loose or strict. Filter value will be ignored");
        } else if (input.getBoolean("strict")) {
            filter = 3;
        } else if (input.getBoolean("loose")) {
            filter = 1;
        }

        ////////////////////
        // Build profiles
        ////////////////////

        if ("PROFILE".equals(input.getString("mode"))) {

            int threadsc = input.getInt("threads");
            threadsc = threadsc > 24 ? 24 : threadsc; //limit max thread count to 24
            Integer radius = input.getInt("radius");

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Building profiles for " + input.getInt("k") + "-mers for a radius of " + radius +  " bp around each read");

            String path = input.get("reads");

            Profile control = new Profile(path, input.getString("genome"), input.getInt("k"), radius, threadsc);
            control.writeProfilesToFile("profile_" + path.split("/")[path.split("/").length - 1] + ".csv");

        ////////////////////
        // Get logo from pre-build profile files
        ////////////////////

        } else if ("LOGO".equals(input.getString("mode"))) {

            int fraglen = input.getInt("fraglen");
            boolean show_gui = input.getBoolean("gui");

            if(input.get("control") == null) {
                LogoHelper.logo(input.getString("signal"), fraglen, show_gui, input.getString("export_kmers"));
            } else {
                LogoHelper.logo(input.getString("control"), input.getString("signal"), fraglen, show_gui, input.getString("export_kmers"));
            }
        }
    }


    public static int getFilter() {
        return filter;
    }
}
