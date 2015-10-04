(ns bpmock-facephi.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [bpmock-facephi.response :as res]
            [cheshire.core :as json]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn get-device
  [{:keys [path-params] :as request}]
  (Thread/sleep 2000)
  (if (= (:deviceid path-params) "001")
    (res/ok {:id "001"
             :type "tablet"
             :created "2015-10-03T17:54:14.004Z"})
    (res/not-found {:message "Dispositivo no registrado"
                    :code "not_found"})))

(defn get-username
  [{:keys [path-params] :as request}]
  (Thread/sleep 2000)
  (case (:username path-params)
    "rosaaviles1604" (res/ok {:username "rosaaviles1604"
                              :devices [{:type "tablet"}
                                        {:type "smartphone"}]})
    "dschuldt" (res/ok {:username "dschuldt"
                        :devices [{:type "smartphone"}]})
    (res/not-found {:message "El usuario no está enrolado en FacePhi Service."
                    :code "not_found"})))

(defn register-device
  [{:keys [path-params] :as request}]
  (Thread/sleep 2000)
  (case (:username path-params)
    "rosaaviles1604" (res/created {:message "El dispositivo fue creado con éxito."})
    "dschuldt" (res/conflict {:message "Este tipo de dispositivo ya existe."
                              :code "conflict"})
    (res/forbidden {:message "Su sesión no está autorizada para acceder este recurso."
                    :code "forbidden"})))

(defn update-device
  [{:keys [path-params] :as request}]
  (Thread/sleep 2000)
  (case (:username path-params)
    "rosaaviles1604" (res/ok {:message "El dispositivo fue actualizado con éxito."})
    "dschuldt" (res/not-found {:message "El dispositivo no existe."
                               :code "not_found"})
    (res/forbidden {:message "Su sesión no está autorizada para acceder este recurso."
                    :code "forbidden"})))

(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params)
                     bootstrap/json-body]
     ["/about" {:get about-page}]
     ["/facephi"
      ["/devices/:deviceid" {:get get-device}]
      ["/users"
       ["/:username" {:get [:get-username get-username]}
        ["/device-registration" {:post [:register-device register-device]}]
        ["/device-update" {:post [:update-device update-device]}]]]]]]])

;; Consumed by bpmock-facephi.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
