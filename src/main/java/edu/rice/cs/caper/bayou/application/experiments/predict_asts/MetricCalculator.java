/*
Copyright 2017 Rice University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.rice.cs.caper.bayou.application.experiments.predict_asts;

import edu.rice.cs.caper.bayou.core.dsl.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MetricCalculator {

    CommandLine cmdLine;

    public MetricCalculator(String[] args) {
        CommandLineParser parser = new DefaultParser();
        org.apache.commons.cli.Options clopts = new org.apache.commons.cli.Options();

        addOptions(clopts);

        try {
            this.cmdLine = parser.parse(clopts, args);
        } catch (ParseException e) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("predict_asts", clopts);
        }
    }

    private void addOptions(org.apache.commons.cli.Options opts) {
        opts.addOption(Option.builder("f")
                .longOpt("input-file")
                .hasArg()
                .numberOfArgs(1)
                .required()
                .desc("input file in JSON with original ASTs, predicted ASTs and sequences")
                .build());
        opts.addOption(Option.builder("m")
                .longOpt("metric")
                .hasArg()
                .numberOfArgs(1)
                .required()
                .desc("metric to use: equality-ast jaccard-sequences jaccard-api-calls num-statements num-control-structures")
                .build());
        opts.addOption(Option.builder("c")
                .longOpt("in-corpus")
                .hasArg()
                .numberOfArgs(1)
                .desc("consider 1:all programs (default), 2:only those in corpus, 3:only those NOT in corpus")
                .build());
        opts.addOption(Option.builder("t")
                .longOpt("top")
                .hasArg()
                .numberOfArgs(1)
                .desc("use the top-k ASTs only")
                .build());
    }

    public void execute() throws IOException {
        if (cmdLine == null)
            return;
        int topk = cmdLine.hasOption("t")? Integer.parseInt(cmdLine.getOptionValue("t")): 10;

        Metric metric;
        String m = cmdLine.getOptionValue("m");
        switch (m) {
            case "equality-ast":
                metric = new EqualityASTMetric();
                break;
            case "jaccard-sequences":
                metric = new JaccardSequencesMetric();
                break;
            case "jaccard-api-calls":
                metric = new JaccardAPICallsMetric();
                break;
            case "num-control-structures":
                metric = new NumControlStructuresMetric();
                break;
            case "num-statements":
                metric = new NumStatementsMetric();
                break;
            default:
                System.err.println("invalid metric: " + cmdLine.getOptionValue("m"));
                return;
        }

        int inCorpus = cmdLine.hasOption("c")? Integer.parseInt(cmdLine.getOptionValue("c")): 1;

        List<JSONInputFormat.DataPoint> data = JSONInputFormat.readData(cmdLine.getOptionValue("f"));
        if (inCorpus == 2)
            data = data.stream().filter(datapoint -> datapoint.in_corpus).collect(Collectors.toList());
        else if (inCorpus == 3)
            data = data.stream().filter(datapoint -> !datapoint.in_corpus).collect(Collectors.toList());

        float value = 0;
        for (JSONInputFormat.DataPoint datapoint : data) {
            DSubTree originalAST = datapoint.ast;
            List<DSubTree> predictedASTs = datapoint.out_asts.subList(0,
                    Math.min(topk, datapoint.out_asts.size()));

            value += metric.compute(originalAST, predictedASTs);
        }
        value /= data.size();
        System.out.println(String.format(
                "Average value of metric %s across %d data points: %f",
                m, data.size(), value));
    }

    public static void main(String args[]) {
        try {
            new MetricCalculator(args).execute();
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
