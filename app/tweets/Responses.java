package tweets;

import utilities.Utilities;

import java.util.*;

/**
 * Represents a list of Response that can be used to retrieve the most relevant
 * Responses based on a set of keywords.
 *
 * @author mathieu
 */

public class Responses {
    private List<Response> responses;
    private int pointer = Math.abs(new Random().nextInt());
    private LinkedList<Response> priorityResponses;

    public Responses() {
        responses = new ArrayList<>();
        priorityResponses = new LinkedList<>();
    }

    /**
     * Adds a new response to the in memory list of responses
     *
     * @param response the response to add
     */
    public void addResponseInMemory(Response response) {
        responses.add(response);
    }

    /**
     * Ads a new response to the in memory representation of responses, and adds it to the json file. The tweet will be
     * present at the next startup of the application.
     *
     * @param newResponse the response to add
     */
    public void addNewResponse(Response newResponse) {
        priorityResponses.add(newResponse);
        Utilities.writeResponse(newResponse);
    }

    /**
     * @param keywords (tags) relevant for a given tweet
     * @param k        number of response that we want to retrieve
     * @return the k most relevant answers
     */
    public List<Response> getTopK(Set<String> keywords, int k) {
        PriorityQueue<Response> respQueue = new PriorityQueue<>();
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

        List<Response> res = new ArrayList<>();

        for (int i = 0; i < k && !respQueue.isEmpty(); ++i) {
            res.add(respQueue.poll());
        }

        return res;
    }

    public Response getFirst(Set<String> keywords) {
        if (!priorityResponses.isEmpty()) {
            Response head = priorityResponses.getFirst();
            responses.add(head);
            return head;
        }

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
            strBuilder.append(resp.toString()).append("\n");
        }
        return strBuilder.toString();
    }
}
