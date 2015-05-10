package utilities;

import play.Logger;

import java.util.*;

/**
 * Represents a list of Response that can be used to retrieve the most relevant
 * Responses based on a set of keywords.
 *
 * @author mathieu
 */

public class Responses {
    private List<Response> responses;
    private int pointer = new Random().nextInt();

    public Responses() {
        responses = new ArrayList<>();
    }

    public void addResponse(Response newResponse) {
        responses.add(newResponse);
    }

    /**
     * @param keywords (tags) relevant for a given tweet
     * @param k        number of response that we want to retrieve
     * @return the k most relevant answers
     */
    public List<Response> getTopK(Set<String> keywords, int k) {
        PriorityQueue<Response> respQueue = new PriorityQueue<Response>();
        for (Response response : responses) {
            Response newResp = new Response(response);
            for (String keyword : keywords) {
                if (newResp.containsTag(keyword)) {
                    newResp.incRank();
                }
            }
            if (newResp.getRank() < 100) {
                respQueue.add(newResp);
            }
        }

        List<Response> res = new ArrayList<Response>();

        for (int i = 0; i < k && !respQueue.isEmpty(); ++i) {
            res.add(respQueue.poll());
        }

        return res;
    }


    public Response getFirst(Set<String> keywords) {
        List<Response> topK = getTopK(keywords, 1);
        if (topK.size() == 0) {
            return responses.get(pointer++ % responses.size());
        } else {
            return topK.get(0);
        }

    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        for (Response resp : responses) {
            strBuilder.append(resp.toString() + "\n");
        }
        return strBuilder.toString();
    }
}
