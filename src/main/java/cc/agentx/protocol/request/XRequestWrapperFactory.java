/*
 * Copyright 2017 ZhangJiupeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.agentx.protocol.request;

public class XRequestWrapperFactory {
    /*private Map<String, Class<? extends XRequestWrapper>> xRequestWrapperList = new HashMap<String, Class<? extends XRequestWrapper>>() {{
        put("shadowsocks", ShadowsocksRequestWrapper.class);
        put("fakedhttp", FakedHttpRequestWrapper.class);
    }};*/

    public static XRequestWrapper getInstance(String id) throws Exception {
        /*Class<? extends XRequestWrapper> clazz = xRequestWrapperList.get(id);
        if (clazz == null) {
            throw new RuntimeException("unknown protocol name: " + id);
        }
        return clazz.newInstance();*/
        switch (id) {
            case "shadowsocks":
                return new ShadowsocksRequestWrapper();
            case "fakedhttp":
                return new FakedHttpRequestWrapper();
            default:
                throw new Exception("unknown protocol \"" + id + "\"");
        }
    }

    public static boolean exists(String id) {
        return id.equals("shadowsocks") || id.equals("fakedhttp");
    }
}
