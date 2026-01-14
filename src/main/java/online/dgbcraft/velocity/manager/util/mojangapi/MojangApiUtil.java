package online.dgbcraft.velocity.manager.util.mojangapi;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.util.GameProfile;
import online.dgbcraft.velocity.manager.VelocityManager;
import online.dgbcraft.velocity.manager.util.HttpUtil;
import online.dgbcraft.velocity.manager.util.gson.typeadapter.UUIDTypeAdapter;
import online.dgbcraft.velocity.manager.util.mojangapi.response.ProfileResultResponse;
import online.dgbcraft.velocity.manager.util.mojangapi.response.ProfileResultsResponse;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

public class MojangApiUtil {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private static final String ROOT_URL = "https://api.mojang.com";
    private static final String USER_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILES_URL = "https://api.mojang.com/profiles/minecraft";

    public static GameProfile getGameProfileByName(String name) throws Exception {
        String result = HttpUtil.getRequest(new URI(USER_PROFILE_URL + name).toURL());
        ProfileResultResponse profile = GSON.fromJson(result, ProfileResultResponse.class);
        if (profile.getError() == null) {
            return profile.getProfile();
        }
        throw new Exception(result);
    }

    public static GameProfile[] getGameProfilesByNames(String[] names) throws Exception {
        Set<String> criteria = Sets.newHashSet();
        for (String name : names) {
            if (!Strings.isNullOrEmpty(name)) {
                criteria.add(name.toLowerCase());
            }
        }
        String payload = VelocityManager.GSON.toJson(criteria);
        String result = HttpUtil.postRequest(new URI(PROFILES_URL).toURL(), payload, "application/json");
        ProfileResultsResponse profiles = GSON.fromJson(result, ProfileResultsResponse.class);
        if (profiles.getError() == null) {
            return profiles.getProfiles();
        }
        throw new Exception(result);
    }
}