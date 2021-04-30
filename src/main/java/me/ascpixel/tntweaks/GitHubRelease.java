package me.ascpixel.tntweaks;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a GitHub release of the plugin.
 */
public class GitHubRelease {
    private final String tagName;

    /**
     * Fetches a GitHub release.
     * @param repo The repository in the format of "author/repo".
     * @param release The target release. "Latest" for the latest
     * @throws IOException Thrown when a connection to GitHub's server could not have been established.
     */
    public GitHubRelease(String repo, String release) throws IOException {
        // Connect to the URL
        final URLConnection connection = new URL("https://api.github.com/repos/" + repo + "/releases/" + release).openConnection();

        // Read everything line by line
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        final StringBuilder content = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null)
        {
            content.append(line).append("\n");
        }
        bufferedReader.close();

        // We now have the API result stored in bufferedReader.
        // The API result is a JSON file; we can parse it with GSON.
        final Map map = new Gson().fromJson(content.toString(), Map.class);

        // Set fields we need.
        tagName = (String) map.get("tag_name");
    }

    /**
     * Gets the tag name of this release.
     */
    public String getTagName(){ return tagName; }

    /**
     * Checks if this plugin's version corresponds to this release's tag name.
     */
    public boolean isPluginVersionThisRelease() {
        return TNTweaks.instance.getDescription().getVersion().equals(getTagName());
    }
}
