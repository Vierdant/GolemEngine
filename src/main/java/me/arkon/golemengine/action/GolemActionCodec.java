package me.arkon.golemengine.action;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class GolemActionCodec implements Codec<GolemAction> {
    private static final String TYPE_KEY = "Type";

    private static final Map<GolemActionTypes, BuilderCodec<? extends GolemAction>> BY_TYPE =
            Map.of(
                    GolemActionTypes.MOVE, MoveAction.CODEC,
                    GolemActionTypes.WAIT, WaitAction.CODEC
            );

    private static final Map<Class<?>, BuilderCodec<? extends GolemAction>> BY_CLASS =
            Map.of(
                    MoveAction.class, MoveAction.CODEC,
                    WaitAction.class, WaitAction.CODEC
            );


    @Override
    public GolemAction decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        BsonDocument doc = bsonValue.asDocument();

        String typeKey = doc.getString(TYPE_KEY).getValue();
        GolemActionTypes type = Arrays.stream(GolemActionTypes.values())
                .filter(t -> t.getKey().equals(typeKey))
                .findFirst()
                .orElseThrow(() -> new CodecException("Unknown GolemAction type: " + typeKey));

        BuilderCodec<? extends GolemAction> codec = BY_TYPE.get(type);
        return codec.decode(doc, extraInfo);
    }


    @Override
    public BsonValue encode(GolemAction action, ExtraInfo extraInfo) {
        BuilderCodec<GolemAction> codec =
                (BuilderCodec<GolemAction>) BY_CLASS.get(action.getClass());

        if (codec == null) {
            throw new CodecException("No codec for action class: " + action.getClass());
        }

        BsonDocument doc = codec.encode(action, extraInfo);
        doc.put(TYPE_KEY, new BsonString(action.getType().getKey()));
        return doc;
    }


    @Override
    public @NotNull Schema toSchema(@NotNull SchemaContext context) {
        // Union schema (editor-friendly)
        return Schema.anyOf(
                MoveAction.CODEC.toSchema(context),
                WaitAction.CODEC.toSchema(context)
        );
    }
}
