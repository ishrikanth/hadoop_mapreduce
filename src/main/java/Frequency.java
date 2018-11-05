import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class Frequency {

    public  static class FrequencyMapper
            extends Mapper<LongWritable, Text, Text, FloatWritable> {


        @Override
        public void map(LongWritable offset, Text lineText, Context context)
                throws IOException, InterruptedException {

            String line = lineText.toString();
            try{
                String eventID = line.split("\t")[1];
                Float beneficiaryDayUniqueCount = Float.parseFloat(line.split("\t")[19]);
                context.write(new Text(eventID), new FloatWritable(beneficiaryDayUniqueCount));
            } catch (Exception e){
                System.out.println("Exception" +e);
            }
        }
    }



    public static class FrequencyReducer extends
            Reducer<Text, FloatWritable, Text, Text> {
        public void reduce(Text key, Iterable<FloatWritable> valueList,
                           Context con) throws IOException, InterruptedException {
            try {
                Float total = (float) 0;
                int count = 0;
                for (FloatWritable var : valueList) {
                    total += var.get();
                    count++;
                }
                Float avg = (Float) total / count;
                String out = "Total: " + total + " :: " + "Average: " + avg;
                con.write(key, new Text(out));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: Frequency <input path> <output path>");
            System.exit(-1);
        }

        // create a Hadoop job and set the main class
        Job job = Job.getInstance();
        job.setJarByClass(Frequency.class);
        job.setJobName("Frequency");

        // set the input and output path
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set the Mapper and Reducer class
        job.setMapperClass(FrequencyMapper.class);
        job.setReducerClass(FrequencyReducer.class);

        // specify the type of the output
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        // run the job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}