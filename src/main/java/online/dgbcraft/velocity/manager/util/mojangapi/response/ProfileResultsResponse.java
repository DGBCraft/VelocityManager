package online.dgbcraft.velocity.manager.util.mojangapi.response;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.velocitypowered.api.util.GameProfile;

import java.lang.reflect.Type;

/**
 * @author Sanluli36li
 */
@JsonAdapter(ProfileResultResponse.Serializer.class)
public class ProfileResultsResponse extends MojangApiResponse {
    private GameProfile[] profiles;

    public GameProfile[] getProfiles() {
        return this.profiles;
    }

    public class Serializer implements JsonDeserializer<ProfileResultsResponse> {
        @Override
        public ProfileResultsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ProfileResultsResponse result = new ProfileResultsResponse();
            if (json instanceof JsonObject object) {
                if (object.has("error")) {
                    result.setError(object.getAsJsonPrimitive("error").getAsString());
                }
                if (object.has("errorMessage")) {
                    result.setError(object.getAsJsonPrimitive("errorMessage").getAsString());
                }
                if (object.has("cause")) {
                    result.setError(object.getAsJsonPrimitive("cause").getAsString());
                }
            } else {
                result.profiles = context.deserialize(json, GameProfile[].class);
            }
            return result;
        }
    }
}