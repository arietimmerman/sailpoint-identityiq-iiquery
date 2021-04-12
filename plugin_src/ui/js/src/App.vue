<template>
  <div id="app">
    <div id="bodyDivTitle">
      <h1>Query Browser</h1>
    </div>

    <div id="wrapper">
      <form @submit="search">
        <textarea
          @keydown="keydown"
          class="form-control"
          placeholder="type a hql query like 'select name from Identity' and press Ctrl+1"
          type="text"
          ref="textarea"
          v-model="hql"
        ></textarea>

        <button class="btn btn-primary" type="submit">Search</button>
        <button
          class="btn btn-secondary"
          @click="showHistory = true"
          :class="{'active': showHistory}"
          type="button"
        >
          History
        </button>

        <div class="advanced">
          Max results
          <input
            class="form-control small"
            v-model.number="limit"
            type="text"
          />
          First row
          <input
            class="form-control small"
            v-model.number="first"
            type="text"
          />
        </div>

        <div class="alert alert-danger" role="alert" v-if="error">
          Errors executing this query.
        </div>
      </form>

      <div v-if="showHistory">
        <div
          class="alert alert-info"
          role="alert"
          v-if="history == null || history.length == 0"
        >
          You have not executed any queries yet.
        </div>

        <table class="history">
          <tr
            v-for="(item, index) in history"
            :key="item.id"
            v-bind:class="['row-' + (index % 2 == 0 ? 'even' : 'odd')]"
            @click="doSearchForHql(item)"
          >
            <td>
              {{ item }}
            </td>
          </tr>
        </table>
      </div>
      <table v-else>
        <tr
          v-for="(item, index) in items"
          :key="item.id"
          v-bind:class="['row-' + (index % 2 == 0 ? 'even' : 'odd')]"
        >
          <td v-for="k in Array.isArray(item) ? item : [item]" :key="k">
            <span v-if="typeof k === 'object' && 'referenceClass' in k">
              {{ k.referenceClass }}({{ k.referenceName || k.id }})
            </span>
            <span v-else>
              {{ k }}
            </span>
          </td>
        </tr>
      </table>
    </div>

    <p class="credits">
      Created by <a target="_blank" href="https://www.linkedin.com/in/arie/">Arie Timmerman</a>
    </p>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "App",
  data() {
    return {
      hql: null,
      error: false,
      items: [],
      limit: 50,
      first: 0,
      showHistory: false,
      history: [],
    };
  },

  mounted() {
    this.history = JSON.parse(localStorage.getItem("hql_history")) || [];

    document.onkeyup = (e) => {
      if (e.ctrlKey && e.key == 1) {
        this.doSearch();
      }
    };
  },
  methods: {
    // Allows using tab in textarea
    keydown(e) {
      if (e.key == "Tab") {
        e.preventDefault();
        var start = this.$refs.textarea.selectionStart;
        var end = this.$refs.textarea.selectionEnd;

        this.$refs.textarea.value =
          this.$refs.textarea.value.substring(0, start) +
          "\t" +
          this.$refs.textarea.value.substring(end);
        this.selectionStart = this.selectionEnd = start + 1;
      }
    },

    doSearchForHql(hql) {
      this.hql = hql;
      this.doSearch();
    },

    doSearch() {
      this.showHistory = false;

      axios
        .post(
          "/identityiq/plugin/rest/iiquery/search",
          {
            hql: this.hql,
            limit: this.limit,
            first: this.first,
          },
          {
            headers: {
              "X-XSRF-TOKEN": window.PluginHelper.getCsrfToken(),
              Accept: "application/json, text/plain, */*",
              "Content-Type": "application/json",
            },
          }
        )
        .then((r) => {
          this.items = r.data;
          this.error = false;

          this.history = this.history.filter((e) => e != this.hql);
          this.history.unshift(this.hql);
          this.history = this.history.slice(0, 50);
          localStorage.setItem("hql_history", JSON.stringify(this.history));
        })
        .catch(() => {
          this.error = true;
        });
    },

    search(e) {
      e.preventDefault();

      this.doSearch();
    },
  },
};
</script>

<style scoped lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 0px;
  position: relative;
}

#wrapper {
  width: calc(100vw - 20px);
  text-align: left;
  padding-left: 20px;

  .form-control {
    display: inline;
  }

  textarea {
    margin-bottom: 4px;
    font-family: "Courier New", Courier, monospace;
    color: black;
  }
}

table {
  margin-top: 20px;
  table-layout: fixed;
  width: 100%;
}

table {

  &.history tr td {
    cursor: pointer;
  }

  tr {
    background-color: white;

    &.row-odd {
      background-color: #fafafa;
      border-style: solid;
      border-color: #ededed transparent #ededed transparent;
      border-width: 1px;
    }

    td {
      padding: 10px;
    }
  }
}
.btn-secondary {
  padding-top: 8px;
  padding-bottom: 8px;
  margin-left: 3px;

  &.active{
    font-weight:bold;
  }
}

.credits,
.alert {
  margin-top: 10px;
}

.advanced {
  float: right;
  input {
    width: 40px;
    text-align: center;
  }
}
</style>
