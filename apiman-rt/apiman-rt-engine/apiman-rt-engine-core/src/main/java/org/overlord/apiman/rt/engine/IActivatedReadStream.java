package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.async.IReadStream;

public interface IActivatedReadStream<H> {
    IReadStream<H> ready();
}
