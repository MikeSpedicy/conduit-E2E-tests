import { Injectable } from '@nestjs/common';
import { Client, mapping, auth } from 'cassandra-driver';

@Injectable()
export class CassandraService {
  client: Client;
  mapper: mapping.Mapper;

  private createClient() {
    // Local Connection
    // this.client = new Client({
    //   contactPoints: ['localhost'],
    //   keyspace: 'conduit',
    //   localDataCenter: 'datacenter1',
    //   authProvider: new auth.PlainTextAuthProvider('cassandra', 'cassandra'),
    // });

    // Astra connection
    this.client = new Client({
      cloud: {
        secureConnectBundle:
          // Astra's DB secure-connect data
          './libs/cassandra-service/src/lib/secure-connect-conduit-test-db.zip',
      },
      credentials: {
        // Astra's clientId value
        username: 'test', // ignore-line
        // Astra's secret value
        password: 'test2', // ignore-line
      },
      // keyspace of your DB
      keyspace: 'conduit_test_db',
    });
  }

  createMapper(mappingOptions: mapping.MappingOptions) {
    if (this.client == undefined) {
      this.createClient();
      this.client.connect();
    }
    return new mapping.Mapper(this.client, mappingOptions);
  }
}
