package online.dgbcraft.velocity.manager.util.mojangapi.response;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.velocitypowered.api.util.GameProfile;

import java.lang.reflect.Type;

/**
 * @author Sanluli36li
 */
@JsonAdapter(ProfileResultResponse.Serializer.class)
public class ProfileResultResponse extends MojangApiResponse {
    private GameProfile profile;

    public GameProfile getProfile() {
        return this.profile;
    }

    public class Serializer implements JsonDeserializer<ProfileResultResponse> {
        @Override
        public ProfileResultResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ProfileResultResponse result = new ProfileResultResponse();
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
                if (object.has("name") && object.has("id")) {
                    result.profile = context.deserialize(json, GameProfile.class);
                }
            }
            return result;
        }
    }
}