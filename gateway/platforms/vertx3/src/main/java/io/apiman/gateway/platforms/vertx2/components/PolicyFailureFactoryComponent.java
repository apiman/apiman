/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Aimport io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
rg/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.platforms.vertx2.components;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;

public class PolicyFailureFactoryComponent implements IPolicyFailureFactoryComponent {

    /**
     * Constructor.
     */
    public PolicyFailureFactoryComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent#createFailure(io.apiman.gateway.engine.beans.PolicyFailureType, int, java.lang.String)
     */
    @Override
    public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
        PolicyFailure failure = new PolicyFailure(); // TODO pool
        failure.setFailureCode(failureCode);
        failure.setMessage(message);
        failure.setType(type);
        return failure;
    }
}
