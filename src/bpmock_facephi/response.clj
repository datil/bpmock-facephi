(ns bpmock-facephi.response
  (:require [ring.util.response :as ring-resp]))

(defn ok
  [body]
  (ring-resp/response body))

(defn not-found
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 404)))

(defn forbidden
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 403)))

(defn unauthorized
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 401)))

(defn created
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 201)))

(defn conflict
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 409)))

(defn bad-request
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 400)))

(defn locked
  [body]
  (-> (ring-resp/response body)
      (ring-resp/status 423)))
