package fr.univ_tours.info.im_olap.graph;


import com.alexsxode.utilities.collection.Pair;
import org.apache.commons.io.Charsets;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.nd4j.linalg.util.MathUtils.log2;


public class Main {

	public static Path WEBLOGS_PATH = Paths.get("./data/WBlogs.csv");


	public static class Bundle {
		public final String userId;
		public final String docID;
		public final int reportID;
		public final String[] objectnames;

		public Bundle(String userId, String docID, int reportID, String[] objectnames) {
			this.userId = userId;
			this.docID = docID;
			this.reportID = reportID;
			this.objectnames = objectnames;
		}

		public static Bundle fromString(String s){
			String[] data = s.split("\\|");

			String[] bundle = Stream.of(data[3].split(","))/*.map(x -> x.trim())*/.toArray(size -> new String[size]);

			return new Bundle(data[0], data[1], Integer.valueOf(data[2]), bundle);
		}

		@Override
		public String toString() {
			return "Bundle{" +
					"userId='" + userId + '\'' +
					", docID='" + docID + '\'' +
					", reportID='" + reportID + '\'' +
					", objectnames=" + Arrays.toString(objectnames) +
					'}';
		}
	}

	public static void main(String[] args) {

	    if (args.length < 5) {
            System.out.println("Usage cl4j.jar blocfile.csv objectfile.csv userfile.csv to_eval.csv m");
            System.exit(1);
        }

	    WEBLOGS_PATH = Paths.get(args[0]);

		try (Stream<String> file = Files.lines(WEBLOGS_PATH, Charsets.ISO_8859_1)) {

			HashMap<String, INDArray> topics = null;
			HashMap<String, INDArray> userVecs = null;
			try {
				topics = WebiQueryPart.readLDAResults(Paths.get(args[1]/*"data/objectnameprofile_lda.csv"*/));

				userVecs = new HashMap<>();
				try (BufferedReader sUserVecs = Files.newBufferedReader(Paths.get(args[2]/*"data/userlda.csv"*/))){
					String s = null;
					while ( (s = sUserVecs.readLine()) != null) {
						String[] tmp = s.split("\\|");

						String[] values = tmp[1].split(",");

                        for (int i = 0; i < values.length; i++) { // trim all front and trailing spaces
                            values[i] = values[i].trim();
                        }

						INDArray arr = Nd4j.zeros(values.length);

						for (int i = 0; i < arr.length(); i++) {
							arr.putScalar(i, Double.parseDouble(values[i]));
						}

						userVecs.put(tmp[0], arr);

					}
				}
			}
			catch (FileNotFoundException e){
				System.out.println("File not found");
			}
			catch (IOException e){
				e.printStackTrace();
			}
			// parse and collect into arraylist
			ArrayList<WebiQueryPart> queryParts = file.map(WebiQueryPart::queryFromCSV)
					.collect(Collectors.toCollection(ArrayList::new));
			System.out.println("Parsed query parts from file. Found " + queryParts.size() + " query parts.");

			System.out.println("Used query parts determined : " /* + usedQueryParts */);

			// Creates the graph
			System.out.println(">>> Calculating graph...");
			NOGraph<Double, String> graph = WebiQueryPart.makeGraph(queryParts);
			System.out.println(">>> Graph completed");

			
			//PageRank.pageRank(graph, 0.95, ignored, iter)
			
			//System.out.println("Beginning test on webi dataset");


			Pair<INDArray, HashMap<String,Integer>> pair = Graphs.toINDMatrix(graph);

			/**
			 *  INPUT PATH
			 */
			//System.out.println("Eval Bundles, Enter filepath:");
			Scanner sc = new Scanner(System.in);
			String path = args[3];
			//String path = "./data/archetypes_lda.csv";

			ArrayList<Bundle> bundles = Files.lines(Paths.get(path))
					.skip(1) // skip header line
					.map(Bundle::fromString)
					.collect(Collectors.toCollection(ArrayList::new));


            System.out.println("Computing scores ...");
            //System.out.println("Format is userid;docid;reportid;score");

            HashMap<String, INDArray> finalUserVecs = userVecs;
            HashMap<String, INDArray> finalTopics = topics;

			IntStream.range(1, 10).parallel().forEach(i -> {
				double alpha = i/10.0;
				INDArray measure = Nd4j.create(bundles.size());
				int j = 0;
				for (Bundle bundle : bundles) {
					try {
						INDArray userVec = finalUserVecs.get(bundle.userId).transpose();

						INDArray biased = PageRank.topicBiasedTPMatrix(finalTopics, pair.getB(), userVec, Double.parseDouble(args[4]));
						HashMap<String, Double> pgRank = PageRank.pageRank(graph, alpha, biased, 50);

						double sum = 0;
						for (String item : bundle.objectnames) {
							sum += -log2(pgRank.get(item));
						}

						measure.putScalar(j++, sum/bundle.objectnames.length);
						//System.out.printf("%s;%s;%d;%f%n", bundle.userId, bundle.docID, bundle.reportID, sum/bundle.objectnames.length);
					} catch (NullPointerException e) {
						System.out.printf("%s;%s;%d;%f%n", bundle.userId, bundle.docID, bundle.reportID, -1f);
					}
				}

				System.out.printf("alpha=%f,mean=%s,dev=%s%n",alpha, measure.mean(1), measure.std(1));
			});


		} catch (IOException e) {
			System.err.println("Could not open file: " + WEBLOGS_PATH);
		}
		System.out.println("[ALL DONE, program terminated.]");
	}

}
